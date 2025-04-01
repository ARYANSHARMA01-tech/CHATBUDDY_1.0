package com.android.chatbuddy.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.chatbuddy.R;
import com.android.chatbuddy.adapters.MessagesAdapter;
import com.android.chatbuddy.models.Chat;
import com.android.chatbuddy.models.Message;
import com.android.chatbuddy.models.User;
import com.android.chatbuddy.utils.FirebaseUtils;
import com.squareup.picasso.Picasso;  // ✅ Import Fixed
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private ImageView profileImage;
    private TextView username;
    private TextView userStatus;
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton btnSend;

    private MessagesAdapter messagesAdapter;
    private List<Message> messageList;

    private String receiverId;
    private String currentUserId;
    private DatabaseReference messagesRef;
    private DatabaseReference chatsRef;
    private ValueEventListener seenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        userStatus = findViewById(R.id.user_status);
        recyclerView = findViewById(R.id.recycler_view);
        messageInput = findViewById(R.id.message_input);
        btnSend = findViewById(R.id.btn_send);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        receiverId = getIntent().getStringExtra("userId");
        currentUserId = FirebaseUtils.getCurrentUserId();

        messagesRef = FirebaseUtils.getMessagesRef();
        chatsRef = FirebaseUtils.getChatsRef();

        btnSend.setOnClickListener(v -> {
            String msg = messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(msg)) {
                sendMessage(currentUserId, receiverId, msg);
            } else {
                Toast.makeText(ChatActivity.this, "You can't send an empty message", Toast.LENGTH_SHORT).show();
            }
            messageInput.setText("");
        });

        FirebaseUtils.getUsersRef().child(receiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());

                if (user.isOnline()) {
                    userStatus.setText("Online");
                } else if (user.getLastSeen() != null) {
                    userStatus.setText("Last seen: " + user.getLastSeen());
                }

                if (user.getImageURL().equals("default")) {
                    profileImage.setImageResource(R.drawable.default_profile);
                } else {
                    Picasso.get().load(user.getImageURL()).into(profileImage);  // ✅ Picasso Fix Applied
                }

                readMessages();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        seenMessage();
    }

    private void seenMessage() {
        seenListener = messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message.getReceiver().equals(currentUserId) && message.getSender().equals(receiverId)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("seen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void sendMessage(String sender, String receiver, String message) {
        DatabaseReference reference = FirebaseUtils.getMessagesRef();
        String messageId = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", messageId);
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("seen", false);
        hashMap.put("timestamp", System.currentTimeMillis());
        hashMap.put("type", "text");

        reference.child(messageId).setValue(hashMap);
        addToChat(sender, receiver, message);
        addToChat(receiver, sender, message);
    }

    private void addToChat(String currentUser, String chatUser, String lastMessage) {
        DatabaseReference chatRef = FirebaseUtils.getChatsRef().child(currentUser).child(chatUser);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userId", chatUser);
        hashMap.put("lastMessage", lastMessage);
        hashMap.put("timestamp", System.currentTimeMillis());
        hashMap.put("seen", currentUser.equals(FirebaseUtils.getCurrentUserId()));

        chatRef.updateChildren(hashMap);
    }

    private void readMessages() {
        messageList = new ArrayList<>();

        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message.getReceiver().equals(currentUserId) && message.getSender().equals(receiverId) ||
                            message.getReceiver().equals(receiverId) && message.getSender().equals(currentUserId)) {
                        messageList.add(message);
                    }
                }

                messagesAdapter = new MessagesAdapter(ChatActivity.this, messageList);
                recyclerView.setAdapter(messagesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
