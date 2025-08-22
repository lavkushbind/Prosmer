// ChatMessage.java
package com.healweal.prosmer;

import com.google.ai.client.generativeai.type.Content;

// A Plain Old Java Object (POJO) for storing message data.
public class ChatMessage {

    private String text;
    private String role; // "user" or "model"
    private long timestamp;

    // A no-argument constructor is required for Firebase Realtime Database deserialization.
    public ChatMessage() {
    }

    public ChatMessage(String text, String role, long timestamp) {
        this.text = text;
        this.role = role;
        this.timestamp = timestamp;
    }

    // --- Getters ---
    public String getText() {
        return text;
    }

    public String getRole() {
        return role;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // --- Setters ---
    public void setText(String text) {
        this.text = text;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }



}