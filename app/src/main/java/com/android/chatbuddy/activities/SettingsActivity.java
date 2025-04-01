package com.android.chatbuddy.activities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.chatbuddy.R;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchNotifications;
    private Switch switchDarkMode;
    private Button btnSave;
    private SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchNotifications = findViewById(R.id.switch_notifications);
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        btnSave = findViewById(R.id.btn_save);

        sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);

        // Load saved settings
        switchNotifications.setChecked(sharedPreferences.getBoolean("notifications", true));
        switchDarkMode.setChecked(sharedPreferences.getBoolean("dark_mode", false));

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notifications", switchNotifications.isChecked());
        editor.putBoolean("dark_mode", switchDarkMode.isChecked());
        editor.apply();
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
    }
}
