package com.android.chatbuddy.models;

public class Message {
    private String id;
    private String sender;
    private String receiver;
    private String message;
    private boolean seen;
    private long timestamp;
    private String type; // text, image, etc.

    public Message() {
        // Required empty constructor for Firebase
    }

    public Message(String sender, String receiver, String message, boolean seen, long timestamp, String type) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.seen = seen;
        this.timestamp = timestamp;
        this.type = type;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    // Fixed the method to return sender ID
    public String getSenderId() {
        return sender;
    }
}
