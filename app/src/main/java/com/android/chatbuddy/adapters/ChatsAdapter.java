package com.android.chatbuddy.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.chatbuddy.R;
import com.android.chatbuddy.activities.ChatActivity;
import com.android.chatbuddy.models.Chat;
import com.android.chatbuddy.models.User;
import com.android.chatbuddy.utils.FirebaseUtils;
import com.android.chatbuddy.utils.TimeUtils;
import com.squareup.picasso.Picasso;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> implements Filterable {
    private Context context;
    private List<Chat> chatList;
    private List<Chat> chatListFull;

    public ChatsAdapter(Context context, List<Chat> chatList) {
        this.context = context;
        this.chatList = chatList;
        this.chatListFull = new ArrayList<>(chatList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Chat chat = chatList.get(position);
        DatabaseReference userRef = FirebaseUtils.getUsersRef().child(chat.getUserId());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    holder.username.setText(user.getUsername());
                    if ("default".equals(user.getImageURL())) {
                        createLetterAvatar(holder.profileImage, user.getUsername());
                    } else {
                        Picasso.get().load(user.getImageURL()).into(holder.profileImage);
                    }
                    holder.imgOnline.setVisibility(user.getOnline() ? View.VISIBLE : View.GONE);
                    holder.imgOffline.setVisibility(user.getOnline() ? View.GONE : View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        holder.lastMessage.setText(chat.getLastMessage());
        holder.timestamp.setText(TimeUtils.getTimeAgo(chat.getTimestamp()));

        if (chat.getUnreadCount() > 0 && !chat.isSeen()) {
            holder.unreadCount.setVisibility(View.VISIBLE);
            holder.unreadCount.setText(String.valueOf(chat.getUnreadCount()));
            holder.lastMessage.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        } else {
            holder.unreadCount.setVisibility(View.GONE);
            holder.lastMessage.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
        }

        holder.itemView.setOnClickListener(v -> {
            FirebaseUtils.getChatsRef().child(FirebaseUtils.getCurrentUserId()).child(chat.getUserId())
                    .child("unreadCount").setValue(0);
            FirebaseUtils.getChatsRef().child(FirebaseUtils.getCurrentUserId()).child(chat.getUserId())
                    .child("seen").setValue(true);
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userId", chat.getUserId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public Filter getFilter() {
        return chatsFilter;
    }

    private final Filter chatsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Chat> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(chatListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Chat chat : chatListFull) {
                    if (chat.getUsername().toLowerCase().contains(filterPattern) ||
                            chat.getLastMessage().toLowerCase().contains(filterPattern)) {
                        filteredList.add(chat);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            chatList.clear();
            chatList.addAll((List<Chat>) results.values);
            notifyDataSetChanged();
        }
    };

    public void updateList(List<Chat> newList) {
        chatList.clear();
        chatList.addAll(newList);
        chatListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    private void createLetterAvatar(ImageView imageView, String username) {
        if (username == null || username.isEmpty()) return;
        String firstLetter = username.substring(0, 1).toUpperCase();
        int size = 100;
        int color = ContextCompat.getColor(context, R.color.colorPrimary);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(color);
        backgroundPaint.setAntiAlias(true);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, backgroundPaint);
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(size / 2f);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        float xPos = size / 2f;
        float yPos = (size / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f);
        canvas.drawText(firstLetter, xPos, yPos, textPaint);
        imageView.setImageBitmap(bitmap);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, lastMessage, timestamp, unreadCount;
        ImageView profileImage;
        View imgOnline, imgOffline;
        public ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            timestamp = itemView.findViewById(R.id.timestamp);
            unreadCount = itemView.findViewById(R.id.unreadCount);
            profileImage = itemView.findViewById(R.id.profileImage);
            imgOnline = itemView.findViewById(R.id.imgOnline);
            imgOffline = itemView.findViewById(R.id.imgOffline);
        }
    }
}