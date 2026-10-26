package com.reyaansh72.reyaanshsstuff;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {

    private String recipientUserId;
    private String recipientUsername;
    private String currentUserId;
    private String currentUsername = "User";
    private String chatId;

    private DatabaseReference messagesRef;
    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;
    private List<MessageItem> messageList;

    private TextInputEditText messageInput;
    private MaterialButton btnSendMessage;
    private ImageButton btnAttach;
    private TextView tvChatRecipientTitle;
    private ImageButton chatDetailBackButton;

    private String replyingToText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_detail);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.chatDetailRoot), (v, insets) -> {
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

        Intent intent = getIntent();
        recipientUserId = intent.getStringExtra("recipient_user_id");
        recipientUsername = intent.getStringExtra("recipient_username");

        if (recipientUserId == null) recipientUserId = "";
        if (recipientUsername == null) recipientUsername = "User";

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser != null ? currentUser.getUid() : "";
        if (currentUser != null && currentUser.getDisplayName() != null) {
            currentUsername = currentUser.getDisplayName();
        } else if (currentUser != null && currentUser.getEmail() != null) {
            currentUsername = currentUser.getEmail().split("@")[0];
        }

        // Deterministic chatId by sorting userIds
        String[] ids = {currentUserId, recipientUserId};
        Arrays.sort(ids);
        chatId = ids[0] + "_" + ids[1];

        messagesRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("messages");

        initializeViews();
        setupRecyclerView();
        loadMessages();
    }

    private void initializeViews() {
        chatDetailBackButton = findViewById(R.id.chatDetailBackButton);
        tvChatRecipientTitle = findViewById(R.id.tvChatRecipientTitle);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnAttach = findViewById(R.id.btnAttach);

        tvChatRecipientTitle.setText("@" + recipientUsername);
        chatDetailBackButton.setOnClickListener(v -> finish());

        btnSendMessage.setOnClickListener(v -> sendMessage("text", null));
        btnAttach.setOnClickListener(v -> showAttachMediaDialog());
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId, this::showMessageActionsDialog);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void loadMessages() {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    MessageItem msg = snap.getValue(MessageItem.class);
                    if (msg != null) {
                        if (msg.getMessageId() == null) {
                            msg.setMessageId(snap.getKey());
                        }
                        messageList.add(msg);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    messagesRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void sendMessage(String mediaType, String mediaUrl) {
        String text = messageInput.getText() != null ? messageInput.getText().toString().trim() : "";
        if (TextUtils.isEmpty(text) && mediaUrl == null) {
            Toast.makeText(this, "Enter a message or attach media", Toast.LENGTH_SHORT).show();
            return;
        }

        String msgId = messagesRef.push().getKey();
        if (msgId == null) {
            msgId = String.valueOf(System.currentTimeMillis());
        }

        MessageItem msg = new MessageItem(
                msgId,
                currentUserId,
                currentUsername,
                text,
                mediaUrl,
                mediaType,
                replyingToText,
                System.currentTimeMillis()
        );

        messagesRef.child(msgId).setValue(msg).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                messageInput.setText("");
                replyingToText = null;
            }
        });
    }

    private void showAttachMediaDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Share File, Image or Video via URL");

        final TextInputEditText input = new TextInputEditText(this);
        input.setHint("Paste Media URL (image, video, file)...");
        builder.setView(input);

        builder.setPositiveButton("Share Image", (dialog, which) -> {
            String url = input.getText() != null ? input.getText().toString().trim() : "";
            if (!url.isEmpty()) sendMessage("image", url);
        });

        builder.setNeutralButton("Share Video", (dialog, which) -> {
            String url = input.getText() != null ? input.getText().toString().trim() : "";
            if (!url.isEmpty()) sendMessage("video", url);
        });

        builder.setNegativeButton("Share File", (dialog, which) -> {
            String url = input.getText() != null ? input.getText().toString().trim() : "";
            if (!url.isEmpty()) sendMessage("file", url);
        });

        builder.show();
    }

    private void showMessageActionsDialog(MessageItem message) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_message_actions, null);
        Button btnReactHeart = dialogView.findViewById(R.id.btnReactHeart);
        Button btnReactThumb = dialogView.findViewById(R.id.btnReactThumb);
        Button btnReactLaugh = dialogView.findViewById(R.id.btnReactLaugh);
        Button btnReactWow = dialogView.findViewById(R.id.btnReactWow);
        MaterialButton btnActionReply = dialogView.findViewById(R.id.btnActionReply);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnReactHeart.setOnClickListener(v -> {
            messagesRef.child(message.getMessageId()).child("reactions").child(currentUserId).setValue("❤️");
            dialog.dismiss();
        });
        btnReactThumb.setOnClickListener(v -> {
            messagesRef.child(message.getMessageId()).child("reactions").child(currentUserId).setValue("👍");
            dialog.dismiss();
        });
        btnReactLaugh.setOnClickListener(v -> {
            messagesRef.child(message.getMessageId()).child("reactions").child(currentUserId).setValue("😂");
            dialog.dismiss();
        });
        btnReactWow.setOnClickListener(v -> {
            messagesRef.child(message.getMessageId()).child("reactions").child(currentUserId).setValue("😮");
            dialog.dismiss();
        });

        btnActionReply.setOnClickListener(v -> {
            replyingToText = message.getText();
            Toast.makeText(this, "Replying to message...", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}