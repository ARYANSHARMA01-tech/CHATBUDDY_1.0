package com.android.chatbuddy.utils;

import com.android.chatbuddy.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseUtils {
    private static FirebaseAuth mAuth;
    private static FirebaseDatabase mDatabase;
    private static FirebaseStorage mStorage;

    // Get Firebase Authentication instance
    public static FirebaseAuth getAuth() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        return mAuth;
    }

    // Get currently logged-in user
    public static FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }

    // Get current user ID
    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // Get Firebase Database instance (Ensure persistence is enabled once)
    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            try {
                mDatabase.setPersistenceEnabled(true);
            } catch (Exception e) {
                // Prevents IllegalStateException if already enabled
            }
        }
        return mDatabase;
    }

    // Get references to Firebase Database nodes
    public static DatabaseReference getUsersRef() {
        return getDatabase().getReference("Users");
    }

    public static DatabaseReference getChatsRef() {
        return getDatabase().getReference("Chats");
    }

    public static DatabaseReference getMessagesRef() {
        return getDatabase().getReference("Messages");
    }

    // Get Firebase Storage instance
    public static FirebaseStorage getStorage() {
        if (mStorage == null) {
            mStorage = FirebaseStorage.getInstance();
        }
        return mStorage;
    }

    // Get reference to profile images storage
    public static StorageReference getProfileImagesRef() {
        return getStorage().getReference().child("profile_images");
    }

    // Update user online status
    public static void updateUserStatus(boolean online) {
        String userId = getCurrentUserId();
        if (userId != null) {
            DatabaseReference userRef = getUsersRef().child(userId);
            try {
                userRef.child("online").setValue(online);
                if (!online) {
                    userRef.child("lastSeen").setValue(System.currentTimeMillis());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Logout user and update status
    public static void logout() {
        updateUserStatus(false);
        getAuth().signOut();
    }
}
