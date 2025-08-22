package com.healweal.prosmer;


import java.util.List;

public class OnboardingQuestion {
    private String questionText;
    private List<String> options;

    public OnboardingQuestion(String questionText, List<String> options) {
        this.questionText = questionText;
        this.options = options;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getOptions() {
        return options;
    }
}
