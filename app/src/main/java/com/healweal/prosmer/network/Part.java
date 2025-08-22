package com.healweal.prosmer.network;

// This class must be public to be accessible by Content
public class Part {

    private String text;

    // Public constructor for Gson and for manual creation in the Request
    public Part(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}