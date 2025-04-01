package com.android.chatbuddy.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.chatbuddy.R;
import com.android.chatbuddy.models.Message;
import com.android.chatbuddy.utils.FirebaseUtils;
import com.android.chatbuddy.utils.TimeUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private List<Message> messageList;
    private String currentUserId;

    public MessagesAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = FirebaseUtils.getCurrentUserId();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageList.get(position);

        if (message.getType().equals("text")) {
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageText.setText(message.getMessage());
            holder.messageImage.setVisibility(View.GONE);
        } else if (message.getType().equals("image")) {
            holder.messageText.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);
            new LoadImageTask(holder.messageImage).execute(message.getMessage());
        }

        holder.timestamp.setText(TimeUtils.getTimeFormatted(message.getTimestamp()));

        // Show "Seen" status only for the last message
        if (position == messageList.size() - 1) {
            if (message.isSeen() && getItemViewType(position) == MSG_TYPE_RIGHT) {
                holder.seenStatus.setVisibility(View.VISIBLE);
                holder.seenStatus.setText(context.getString(R.string.seen)); // Ensure this string exists in strings.xml
            } else {
                holder.seenStatus.setVisibility(View.GONE);
            }
        } else {
            holder.seenStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getSenderId().equals(currentUserId) ? MSG_TYPE_RIGHT : MSG_TYPE_LEFT;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public ImageView messageImage;
        public TextView timestamp;
        public TextView seenStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageImage = itemView.findViewById(R.id.message_image);
            timestamp = itemView.findViewById(R.id.timestamp);
            seenStatus = itemView.findViewById(R.id.seen_status);
        }
    }

    // AsyncTask to load images without external libraries
    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            Bitmap bitmap = null;
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.placeholder_image); // Ensure placeholder_image exists
            }
        }
    }
}
