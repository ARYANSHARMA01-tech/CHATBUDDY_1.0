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
import com.android.chatbuddy.models.Message;
import com.android.chatbuddy.models.User;
import com.android.chatbuddy.utils.FirebaseUtils;
import com.squareup.picasso.Picasso;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private ImageView profileImage;
    private TextView username, userStatus;
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton btnSend;

    private MessagesAdapter messagesAdapter;
    private List<Message> messageList;

    private String receiverId, currentUserId;
    private DatabaseReference messagesRef, chatsRef;
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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        receiverId = getIntent().getStringExtra("userId");
        currentUserId = FirebaseUtils.getCurrentUserId();

        messagesRef = FirebaseUtils.getMessagesRef();
        chatsRef = FirebaseUtils.getChatsRef();

        btnSend.setOnClickListener(v -> sendMessage());
        loadReceiverInfo();
        readMessages();
        seenMessage();
    }

    private void loadReceiverInfo() {
        FirebaseUtils.getUsersRef().child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    username.setText(user.getUsername());
                    userStatus.setText(user.isOnline() ? "Online" : "Last seen: " + user.getLastSeen());
                    if ("default".equals(user.getImageURL())) {
                        profileImage.setImageResource(R.drawable.default_profile);
                    } else {
                        Picasso.get().load(user.getImageURL()).into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void seenMessage() {
        seenListener = messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Message message = snap.getValue(Message.class);
                    if (message != null && message.getReceiver().equals(currentUserId) && message.getSender().equals(receiverId)) {
                        snap.getRef().child("seen").setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendMessage() {
        String msg = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(msg)) {
            Toast.makeText(this, "You can't send an empty message", Toast.LENGTH_SHORT).show();
            return;
        }
        messageInput.setText("");

        String messageId = messagesRef.push().getKey();
        if (messageId == null) return; // Prevent null ID issue

        Message message = new Message(messageId, currentUserId, receiverId, msg, false, System.currentTimeMillis(), "text");
        messagesRef.child(messageId).setValue(message)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    }
                });

        updateChatList();
    }

    private void updateChatList() {
        DatabaseReference chatRef = chatsRef.child(currentUserId).child(receiverId);
        chatRef.child("lastMessage").setValue(System.currentTimeMillis());
        chatRef.child("seen").setValue(false);
    }

    private void readMessages() {
        messageList = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(ChatActivity.this, messageList);
        recyclerView.setAdapter(messagesAdapter);

        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Message message = snap.getValue(Message.class);
                    if (message != null && (message.getReceiver().equals(currentUserId) && message.getSender().equals(receiverId) ||
                            message.getReceiver().equals(receiverId) && message.getSender().equals(currentUserId))) {
                        messageList.add(message);
                    }
                }
                messagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (seenListener != null) {
            messagesRef.removeEventListener(seenListener);
        }
    }
}
