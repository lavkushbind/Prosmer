package com.healweal.prosmer.network;

import java.util.List;

// This class must be public to be accessible by other classes
public class Content {

    private List<Part> parts;

    // Public constructor for Gson and for manual creation in the Request
    public Content(List<Part> parts) {
        this.parts = parts;
    }

    public List<Part> getParts() {
        return parts;
    }
}