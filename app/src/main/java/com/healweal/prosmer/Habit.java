package com.healweal.prosmer;
// In your main package
public class Habit {
    private String id; // Firestore document ID
    private String name;
    private boolean isCompleted;

    public Habit(String id, String name, boolean isCompleted) {
        this.id = id;
        this.name = name;
        this.isCompleted = isCompleted;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}