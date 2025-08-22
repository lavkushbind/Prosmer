package com.healweal.prosmer; // Use your package name

import java.util.List;

public class MoodEntry {
    public String mood;
    public int intensity;
    public List<String> tags;
    public long timestamp; // Use 'long' for timestamp in Realtime DB

    // Default constructor is required for calls to DataSnapshot.getValue(MoodEntry.class)
    public MoodEntry() {
    }

    public MoodEntry(String mood, int intensity, List<String> tags) {
        this.mood = mood;
        this.intensity = intensity;
        this.tags = tags;
    }

    // Getters and Setters are good practice but public fields work too
}