package com.healweal.prosmer.network;


import java.util.Collections;
import java.util.List;

public class GeminiRequest {

    // Field is no longer final to be a standard POJO
    private List<Content> contents;

    // The constructor remains for easy object creation
    public GeminiRequest(String text) {
        Part part = new Part(text);
        Content content = new Content(Collections.singletonList(part));
        this.contents = Collections.singletonList(content);
    }

    // Getter for serialization
    public List<Content> getContents() {
        return contents;
    }
}