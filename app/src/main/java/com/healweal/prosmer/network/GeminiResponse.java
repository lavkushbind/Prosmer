package com.healweal.prosmer.network;

import java.util.List;

public class GeminiResponse {

    private List<Candidate> candidates;

    // Getter for serialization and access
    public List<Candidate> getCandidates() {
        return candidates;
    }

    /**
     * A helper method to safely extract the response text.
     * This is excellent logic and has been preserved.
     */
    public String getResponseText() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate firstCandidate = candidates.get(0);
            if (firstCandidate != null) {
                Content content = firstCandidate.getContent();
                if (content != null && content.getParts() != null && !content.getParts().isEmpty()) {
                    Part firstPart = content.getParts().get(0);
                    if (firstPart != null) {
                        return firstPart.getText();
                    }
                }
            }
        }
        // Return a default message if the structure is not as expected
        return "Sorry, I couldn't process that response.";
    }
}