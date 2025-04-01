package com.android.chatbuddy;

import android.app.Application;
import android.content.Context;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;

public class ChatBuddyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        initializeFirebase();
    }

    private void initializeFirebase() {
        // Initialize Firebase Database with offline capabilities
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        database.setPersistenceCacheSizeBytes(10 * 1024 * 1024); // 10MB cache

        // Initialize Firebase Remote Config with default values
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        // Configure Remote Config settings
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600) // 1 hour for development
                .build();

        remoteConfig.setConfigSettingsAsync(configSettings);

        // Set in-memory defaults as fallback if XML file doesn't exist
        remoteConfig.setDefaultsAsync(new HashMap<String, Object>() {{
            put("welcome_message", "Welcome to ChatBuddy!");
            put("feature_enabled", true);
        }});
    }

    public static Context getAppContext() {
        return context;
    }
}