package com.android.chatbuddy.models;

public class User {
    private String id;
    private String username;
    private String email;
    private String imageURL;
    private String status;
    private String search;
    private boolean online;
    private String lastSeen;

    public User() {
        // Required empty constructor for Firebase
    }

    public User(String id, String username, String email, String imageURL, String status, boolean online, String lastSeen) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.imageURL = imageURL;
        this.status = status;
        this.search = username.toLowerCase();
        this.online = online;
        this.lastSeen = lastSeen;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) {
        this.username = username;
        this.search = username.toLowerCase();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getImageURL() { return imageURL; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public String getLastSeen() { return lastSeen; }
    public void setLastSeen(String lastSeen) { this.lastSeen = lastSeen; }

    public boolean getOnline() { return online; }
}
