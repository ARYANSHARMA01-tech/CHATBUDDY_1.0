package com.android.chatbuddy.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.chatbuddy.R;
import com.android.chatbuddy.adapters.ChatsAdapter;
import com.android.chatbuddy.models.Chat;
import com.android.chatbuddy.utils.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ChatsAdapter chatsAdapter;
    private List<Chat> chatList;
    private TextView noChatsText;
    private ProgressBar progressBar;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        noChatsText = view.findViewById(R.id.empty_view);
        progressBar = view.findViewById(R.id.progress_bar);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        currentUserId = FirebaseUtils.getCurrentUserId();

        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e("ChatsFragment", "Error: currentUserId is null or empty.");
            return view; // Prevent readChats() from running
        }

        chatList = new ArrayList<>();
        chatsAdapter = new ChatsAdapter(getContext(), chatList);
        recyclerView.setAdapter(chatsAdapter);

        readChats(); // Call only if currentUserId is valid

        return view;
    }

    private void readChats() {
        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e("ChatsFragment", "Error: currentUserId is null or empty in readChats()");
            return;
        }

        DatabaseReference reference = FirebaseUtils.getChatsRef().child(currentUserId);
        progressBar.setVisibility(View.VISIBLE); // Show loading indicator

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat != null) {
                        chatList.add(chat);
                    }
                }

                progressBar.setVisibility(View.GONE); // Hide progress bar after loading

                if (chatList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    noChatsText.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    noChatsText.setVisibility(View.GONE);

                    // Sort chats by timestamp (newest first)
                    Collections.sort(chatList, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));

                    chatsAdapter.notifyDataSetChanged(); // Refresh adapter
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Log.e("ChatsFragment", "Failed to load chats: " + databaseError.getMessage());
            }
        });
    }

    public void searchChats(String searchText) {
        if (chatsAdapter != null) {
            chatsAdapter.getFilter().filter(searchText);
        }
    }
}
