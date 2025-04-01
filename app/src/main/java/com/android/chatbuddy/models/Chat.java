package com.android.chatbuddy.models;

public class Chat {
    private String id;
    private String userId;
    private String username;
    private String lastMessage;
    private boolean seen;
    private long timestamp;
    private int unreadCount = 0; // Default to prevent null issues

    // Required empty constructor for Firebase
    public Chat() {}

    // Constructor without unreadCount (default to 0)
    public Chat(String id, String userId, String username, String lastMessage, boolean seen, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.lastMessage = lastMessage;
        this.seen = seen;
        this.timestamp = timestamp;
        this.unreadCount = 0; // Default
    }

    // Constructor with unreadCount parameter
    public Chat(String id, String userId, String username, String lastMessage, boolean seen, long timestamp, int unreadCount) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.lastMessage = lastMessage;
        this.seen = seen;
        this.timestamp = timestamp;
        this.unreadCount = unreadCount;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void incrementUnreadCount() {
        this.unreadCount++;
    }

    public void resetUnreadCount() {
        this.unreadCount = 0;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                ", seen=" + seen +
                ", timestamp=" + timestamp +
                ", unreadCount=" + unreadCount +
                '}';
    }
}
