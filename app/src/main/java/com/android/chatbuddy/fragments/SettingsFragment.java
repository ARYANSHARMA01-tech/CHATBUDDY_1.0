package com.android.chatbuddy.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.chatbuddy.R;
import com.android.chatbuddy.activities.ProfileActivity;
import com.android.chatbuddy.models.User;
import com.android.chatbuddy.utils.FirebaseUtils;
import com.squareup.picasso.Picasso;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class SettingsFragment extends Fragment {
    private ImageView profileImage;
    private TextView username, status;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        profileImage = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);
        status = view.findViewById(R.id.status);
        Button btnEditProfile = view.findViewById(R.id.btn_edit_profile);

        // Load user data
        String currentUserId = FirebaseUtils.getCurrentUserId();
        assert currentUserId != null;
        DatabaseReference reference = FirebaseUtils.getUsersRef().child(currentUserId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                assert user != null;
                username.setText(user.getUsername());
                status.setText(user.getStatus());

                if (user.getImageURL().equals("default")) {
                    profileImage.setImageResource(R.drawable.default_profile);
                } else {
                    if (getContext() != null) {
                        Picasso.get()
                                .load(user.getImageURL())
                                .placeholder(R.drawable.default_profile) // Placeholder while loading
                                .error(R.drawable.default_profile) // In case of error
                                .transform(new com.android.chatbuddy.utils.CircleTransform()) // Custom transformation for circular image
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        // Set click listener for edit profile button
        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(getContext(), ProfileActivity.class)));

        return view;
    }
}
