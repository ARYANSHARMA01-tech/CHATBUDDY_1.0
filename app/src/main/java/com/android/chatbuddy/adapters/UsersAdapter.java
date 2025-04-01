package com.android.chatbuddy.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.chatbuddy.R;
import com.android.chatbuddy.activities.ChatActivity;
import com.android.chatbuddy.models.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> implements Filterable {
    private Context context;
    private List<User> userList;
    private List<User> userListFull;

    public UsersAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        this.userListFull = new ArrayList<>(userList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = userList.get(position);
        holder.username.setText(user.getUsername());
        holder.status.setText(user.getStatus());

        if (user.getImageURL().equals("default")) {
            holder.profileImage.setImageResource(R.drawable.default_profile);
        } else {
            Picasso.get().load(user.getImageURL()).into(holder.profileImage);
        }

        // Show online/offline indicators
        if (user.isOnline()) {
            holder.imgOnline.setVisibility(View.VISIBLE);
            holder.imgOffline.setVisibility(View.GONE);
        } else {
            holder.imgOnline.setVisibility(View.GONE);
            holder.imgOffline.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userId", user.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileImage;
        public TextView username;
        public TextView status;
        public View imgOnline;
        public View imgOffline;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
            status = itemView.findViewById(R.id.status);
            imgOnline = itemView.findViewById(R.id.img_online);
            imgOffline = itemView.findViewById(R.id.img_offline);
        }
    }

    @Override
    public Filter getFilter() {
        return userFilter;
    }

    private final Filter userFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<User> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(userListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (User user : userListFull) {
                    if (user.getUsername().toLowerCase().contains(filterPattern) ||
                            user.getStatus().toLowerCase().contains(filterPattern)) {
                        filteredList.add(user);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            userList.clear();
            userList.addAll((List<User>) results.values);
            notifyDataSetChanged();
        }
    };
}
