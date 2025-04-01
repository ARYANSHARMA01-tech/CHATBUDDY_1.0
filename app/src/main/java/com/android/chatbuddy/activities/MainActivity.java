package com.android.chatbuddy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.android.chatbuddy.R;import com.android.chatbuddy.fragments.ChatsFragment;
import com.android.chatbuddy.fragments.SettingsFragment;
import com.android.chatbuddy.fragments.UsersFragment;
import com.android.chatbuddy.utils.FirebaseUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private ChatsFragment chatsFragment;
    private UsersFragment usersFragment;
    private SettingsFragment settingsFragment;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        FloatingActionButton fabNewChat = findViewById(R.id.fab_new_chat);

        chatsFragment = new ChatsFragment();
        usersFragment = new UsersFragment();
        settingsFragment = new SettingsFragment();

        loadFragment(chatsFragment);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_chats) {
                selectedFragment = chatsFragment;
            } else if (item.getItemId() == R.id.nav_users) {
                selectedFragment = usersFragment;
            } else if (item.getItemId() == R.id.nav_settings) {
                selectedFragment = settingsFragment;
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });

        fabNewChat.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ChatActivity.class))
        );
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (currentFragment instanceof ChatsFragment) {
                        chatsFragment.searchChats(newText);
                    } else if (currentFragment instanceof UsersFragment) {
                        usersFragment.searchUsers(newText);
                    }
                    return true;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            FirebaseUtils.logout();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        } else if (id == R.id.action_profile) {
            loadFragment(new SettingsFragment());  // Profile moved to settings
            return true;
        } else if (id == R.id.action_signup) {
            showSignupDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSignupDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Up")
                .setMessage("Do you want to sign up?")
                .setPositiveButton("Yes", (dialog, which) ->
                        startActivity(new Intent(MainActivity.this, SignupActivity.class)))
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUtils.updateUserStatus(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtils.updateUserStatus(false);
    }
}
