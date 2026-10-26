package com.reyaansh72.reyaanshsstuff;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AiChatActivity extends AppCompatActivity {

    private Spinner spinnerModels;
    private RecyclerView aiChatRecyclerView;
    private AiMessageAdapter aiMessageAdapter;
    private List<AiMessageItem> messageList;
    private TextInputEditText etAiPrompt;

    private String ollamaHost = "http://10.0.2.2:11434";
    private final List<String> availableModels = new ArrayList<>();
    private ArrayAdapter<String> modelAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ai_chat);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.aiChatRoot), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );
                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );
                    return insets;
                }
        );

        ollamaHost = ThemeUtils.getOllamaHostUrl(this);

        ImageButton aiBackButton = findViewById(R.id.aiBackButton);
        aiBackButton.setOnClickListener(v -> finish());

        spinnerModels = findViewById(R.id.spinnerModels);
        MaterialButton btnPullModel = findViewById(R.id.btnPullModel);
        aiChatRecyclerView = findViewById(R.id.aiChatRecyclerView);
        etAiPrompt = findViewById(R.id.etAiPrompt);
        MaterialButton btnSendAi = findViewById(R.id.btnSendAi);

        setupRecyclerView();

        availableModels.add("llama3.2");
        availableModels.add("mistral");
        availableModels.add("codellama");
        modelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, availableModels);
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModels.setAdapter(modelAdapter);

        fetchOllamaModels();

        btnPullModel.setOnClickListener(v -> showPullModelDialog());
        btnSendAi.setOnClickListener(v -> handleUserPrompt());

        addAiMessage("Hello! I am your local Ollama AI Agent & Coding Assistant. Ask me anything, generate code, or tell me to perform agentic tasks (e.g. 'create short title Test url https://...').");
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        aiMessageAdapter = new AiMessageAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        aiChatRecyclerView.setLayoutManager(layoutManager);
        aiChatRecyclerView.setAdapter(aiMessageAdapter);
    }

    private void addAiMessage(String text) {
        messageList.add(new AiMessageItem(text, false, System.currentTimeMillis()));
        aiMessageAdapter.notifyItemInserted(messageList.size() - 1);
        aiChatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
    }

    private void addUserMessage(String text) {
        messageList.add(new AiMessageItem(text, true, System.currentTimeMillis()));
        aiMessageAdapter.notifyItemInserted(messageList.size() - 1);
        aiChatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
    }

    private void fetchOllamaModels() {
        new Thread(() -> {
            try {
                String response = httpGet(ollamaHost + "/api/tags");
                JSONObject json = new JSONObject(response);
                JSONArray models = json.getJSONArray("models");

                List<String> fetched = new ArrayList<>();
                for (int i = 0; i < models.length(); i++) {
                    JSONObject m = models.getJSONObject(i);
                    fetched.add(m.optString("name", "model"));
                }

                if (!fetched.isEmpty()) {
                    runOnUiThread(() -> {
                        availableModels.clear();
                        availableModels.addAll(fetched);
                        modelAdapter.notifyDataSetChanged();
                    });
                }
            } catch (Exception e) {
                // Offline or unreachable
            }
        }).start();
    }

    private void showPullModelDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null);
        TextInputEditText modelInput = dialogView.findViewById(R.id.addUsernameInput);
        modelInput.setHint("Model name (e.g. qwen2.5:7b)");
        MaterialButton btnPull = dialogView.findViewById(R.id.btnAddContactAction);
        btnPull.setText("Download Model");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnPull.setOnClickListener(v -> {
            String modelName = modelInput.getText() != null ? modelInput.getText().toString().trim() : "";
            if (!TextUtils.isEmpty(modelName)) {
                dialog.dismiss();
                Toast.makeText(this, "Pulling model " + modelName + " from Ollama...", Toast.LENGTH_LONG).show();
                pullOllamaModel(modelName);
            }
        });

        dialog.show();
    }

    private void pullOllamaModel(String modelName) {
        new Thread(() -> {
            try {
                String payload = "{\"name\": \"" + modelName + "\"}";
                httpPost(ollamaHost + "/api/pull", payload);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Model " + modelName + " pulled successfully!", Toast.LENGTH_SHORT).show();
                    fetchOllamaModels();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Failed to pull model: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void handleUserPrompt() {
        String prompt = etAiPrompt.getText() != null ? etAiPrompt.getText().toString().trim() : "";
        if (TextUtils.isEmpty(prompt)) return;

        etAiPrompt.setText("");
        addUserMessage(prompt);

        String selectedModel = spinnerModels.getSelectedItem() != null ? spinnerModels.getSelectedItem().toString() : "llama3.2";

        // 1. Check for Agentic Workflows
        String lower = prompt.toLowerCase();
        if (lower.contains("create short") || lower.contains("publish short")) {
            executeAgentCreateShort(prompt);
            return;
        }

        if (lower.contains("create note") || lower.contains("new note")) {
            executeAgentCreateNote(prompt);
            return;
        }

        // 2. Normal Chat & Coding Assistance via Ollama API
        queryOllamaChat(selectedModel, prompt);
    }

    private void executeAgentCreateShort(String prompt) {
        addAiMessage("🤖 [Agent Working] Parsing request and publishing Short to Firebase Feed...");

        final String videoUrl;
        if (prompt.contains("http://") || prompt.contains("https://")) {
            int idx = prompt.indexOf("http");
            String[] parts = prompt.substring(idx).split(" ");
            videoUrl = parts[0];
        } else {
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4";
        }

        String title = "AI Generated Short 🤖";

        DatabaseReference shortsRef = FirebaseDatabase.getInstance().getReference("shorts");
        String shortId = shortsRef.push().getKey();
        if (shortId == null) shortId = String.valueOf(System.currentTimeMillis());

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "agent";
        String username = FirebaseAuth.getInstance().getCurrentUser() != null && FirebaseAuth.getInstance().getCurrentUser().getDisplayName() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "AI Agent";

        ShortItem item = new ShortItem(shortId, title, prompt, videoUrl, "", username, userId, System.currentTimeMillis(), true);

        shortsRef.child(shortId).setValue(item).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                addAiMessage("✅ [Agent Action Complete] Successfully created and published short '" + title + "' with URL: " + videoUrl);
            } else {
                addAiMessage("❌ [Agent Error] Failed to publish short to Firebase.");
            }
        });
    }

    private void executeAgentCreateNote(String prompt) {
        addAiMessage("🤖 [Agent Working] Creating personal note in database...");

        String title = "AI Note";

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("notes").child(uid);
        String noteId = notesRef.push().getKey();
        if (noteId == null) noteId = String.valueOf(System.currentTimeMillis());

        NoteItem note = new NoteItem(noteId, title, prompt, System.currentTimeMillis());
        notesRef.child(noteId).setValue(note).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                addAiMessage("✅ [Agent Action Complete] Successfully created personal note!");
            } else {
                addAiMessage("❌ [Agent Error] Failed to save note.");
            }
        });
    }

    private void queryOllamaChat(String model, String prompt) {
        new Thread(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("model", model);
                payload.put("prompt", prompt);
                payload.put("stream", false);

                String resJson = httpPost(ollamaHost + "/api/generate", payload.toString());
                JSONObject resObj = new JSONObject(resJson);
                String aiReply = resObj.optString("response", "No response from model.");

                runOnUiThread(() -> addAiMessage(aiReply));
            } catch (Exception e) {
                runOnUiThread(() -> addAiMessage("⚠️ Failed to connect to Ollama at " + ollamaHost + ". Check your Settings for Ollama Host URL.\nError: " + e.getMessage()));
            }
        }).start();
    }

    private String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(6000);
        conn.setReadTimeout(6000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();
        return sb.toString();
    }

    private String httpPost(String urlStr, String jsonBody) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();
        return sb.toString();
    }
}