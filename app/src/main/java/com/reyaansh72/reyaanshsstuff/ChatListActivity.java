package com.reyaansh72.reyaanshsstuff;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private DatabaseReference contactsRef;
    private String currentUserId;

    private ImageButton chatBackButton;
    private MaterialButton btnAddContactHeader;
    private FloatingActionButton fabAddContact;
    private TextView tvNoContacts;
    private RecyclerView contactsRecyclerView;

    private List<UserItem> contactList;
    private ContactsAdapter contactsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_list);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.chatTopBar), (v, insets) -> {
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

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        contactsRef = FirebaseDatabase.getInstance().getReference("contacts").child(currentUserId);

        initializeViews();
        setupRecyclerView();
        loadContacts();
    }

    private void initializeViews() {
        chatBackButton = findViewById(R.id.chatBackButton);
        btnAddContactHeader = findViewById(R.id.btnAddContactHeader);
        fabAddContact = findViewById(R.id.fabAddContact);
        tvNoContacts = findViewById(R.id.tvNoContacts);
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);

        chatBackButton.setOnClickListener(v -> finish());
        btnAddContactHeader.setOnClickListener(v -> showAddContactDialog());
        fabAddContact.setOnClickListener(v -> showAddContactDialog());
    }

    private void setupRecyclerView() {
        contactList = new ArrayList<>();
        contactsAdapter = new ContactsAdapter(contactList, contact -> {
            Intent intent = new Intent(ChatListActivity.this, ChatDetailActivity.class);
            intent.putExtra("recipient_user_id", contact.getUid());
            intent.putExtra("recipient_username", contact.getUsername());
            startActivity(intent);
        });
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactsRecyclerView.setAdapter(contactsAdapter);
    }

    private void loadContacts() {
        if (currentUserId.isEmpty()) return;

        contactsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> contactIds = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    contactIds.add(snap.getKey());
                }

                if (contactIds.isEmpty()) {
                    contactList.clear();
                    contactsAdapter.notifyDataSetChanged();
                    tvNoContacts.setVisibility(View.VISIBLE);
                    contactsRecyclerView.setVisibility(View.GONE);
                    return;
                }

                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot usersSnap) {
                        contactList.clear();
                        for (DataSnapshot userSnap : usersSnap.getChildren()) {
                            UserItem user = userSnap.getValue(UserItem.class);
                            if (user != null && contactIds.contains(user.getUid())) {
                                contactList.add(user);
                            }
                        }
                        contactsAdapter.notifyDataSetChanged();

                        if (contactList.isEmpty()) {
                            tvNoContacts.setVisibility(View.VISIBLE);
                            contactsRecyclerView.setVisibility(View.GONE);
                        } else {
                            tvNoContacts.setVisibility(View.GONE);
                            contactsRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showAddContactDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null);
        TextInputEditText usernameInput = dialogView.findViewById(R.id.addUsernameInput);
        MaterialButton btnAdd = dialogView.findViewById(R.id.btnAddContactAction);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnAdd.setOnClickListener(v -> {
            String targetUsername = usernameInput.getText() != null ? usernameInput.getText().toString().trim().replace("@", "") : "";
            if (TextUtils.isEmpty(targetUsername)) {
                Toast.makeText(this, "Enter a username", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mAuth.getCurrentUser() != null && targetUsername.equalsIgnoreCase(mAuth.getCurrentUser().getDisplayName())) {
                Toast.makeText(this, "You cannot add yourself as a contact", Toast.LENGTH_SHORT).show();
                return;
            }

            usersRef.orderByChild("username").equalTo(targetUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(ChatListActivity.this, "User '@" + targetUsername + "' not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        UserItem foundUser = userSnap.getValue(UserItem.class);
                        if (foundUser != null) {
                            contactsRef.child(foundUser.getUid()).setValue(true).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ChatListActivity.this, "Added @" + foundUser.getUsername() + " to contacts!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(ChatListActivity.this, "Failed to add contact", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        });

        dialog.show();
    }
}