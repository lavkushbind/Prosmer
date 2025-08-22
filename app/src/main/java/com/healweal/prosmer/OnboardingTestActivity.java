package com.healweal.prosmer; // Use your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnboardingTestActivity extends AppCompatActivity {

    // Enum for managing UI state
    private enum State {
        INTRO, QUESTION, SUMMARY
    }
    private State currentState = State.INTRO;

    // UI Elements
    private ViewGroup rootLayout;
    private View introGroup;
    private View questionGroup;
    private View summaryGroup;
    private MaterialButton btnNext, btnViewPlan;
    private TextView btnSkip, tvProgressCounter, tvQuestion;
    private ProgressBar progressBar;

    // Question Logic
    private List<OnboardingQuestion> questionsList;
    private int currentQuestionIndex = 0;
    private Map<String, String> userAnswers = new HashMap<>();

    // Option Card Views
    private List<MaterialCardView> optionCards;
    private List<TextView> optionTextViews; // To hold references to the TextViews inside cards
    private MaterialCardView selectedCard = null;

    // Firebase
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_test);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            // Handle user not logged in case
            Toast.makeText(this, "Error: User not authenticated.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize UI
        initViews();
        setupQuestions();
        setupClickListeners();

        // Set initial UI state
        updateUiForState();
    }

    private void initViews() {
        rootLayout = findViewById(android.R.id.content); // For transitions
        introGroup = findViewById(R.id.introGroup);
        questionGroup = findViewById(R.id.questionGroup);
        summaryGroup = findViewById(R.id.summaryGroup);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
        tvProgressCounter = findViewById(R.id.tvProgressCounter);
        tvQuestion = findViewById(R.id.tvQuestion);
        progressBar = findViewById(R.id.progressBar);
        btnViewPlan = findViewById(R.id.btnViewPlan); // Button inside summary card

        // More robustly find cards and their text views
        optionCards = Arrays.asList(
                findViewById(R.id.cardOption1),
                findViewById(R.id.cardOption2),
                findViewById(R.id.cardOption3),
                findViewById(R.id.cardOption4),
                findViewById(R.id.cardOption5)
        );
        optionTextViews = Arrays.asList(
                findViewById(R.id.tvOptionText1),
                findViewById(R.id.tvOptionText2),
                findViewById(R.id.tvOptionText3),
                findViewById(R.id.tvOptionText4),
                findViewById(R.id.tvOptionText5)
        );
    }

    private void setupQuestions() {
        questionsList = new ArrayList<>();
        questionsList.add(new OnboardingQuestion("How often do you feel stressed during a typical week?", Arrays.asList("Rarely", "Sometimes", "Often", "Almost Always")));
        questionsList.add(new OnboardingQuestion("What is your primary goal for using this app?", Arrays.asList("Reduce anxiety", "Improve focus", "Sleep better", "Be more mindful")));
        questionsList.add(new OnboardingQuestion("How would you rate your current sleep quality?", Arrays.asList("Excellent", "Good", "Fair", "Poor")));
        questionsList.add(new OnboardingQuestion("How much time can you dedicate daily?", Arrays.asList("Less than 5 mins", "5-10 mins", "10-20 mins", "20+ mins")));
        questionsList.add(new OnboardingQuestion("Which activity interests you the most?", Arrays.asList("Guided Meditation", "Journaling", "Breathing Exercises", "AI Coaching")));
    }

    private void setupClickListeners() {
        btnNext.setOnClickListener(v -> handleNextClick());
        btnSkip.setOnClickListener(v -> navigateToNextScreen());
        btnViewPlan.setOnClickListener(v -> navigateToNextScreen()); // Summary button also navigates
        for (MaterialCardView card : optionCards) {
            card.setOnClickListener(this::onOptionCardClicked);
        }
    }

    private void handleNextClick() {
        if (currentState == State.INTRO) {
            currentState = State.QUESTION;
            displayQuestion(currentQuestionIndex);
        } else if (currentState == State.QUESTION) {
            if (selectedCard == null) {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save answer robustly
            String question = questionsList.get(currentQuestionIndex).getQuestionText();
            int selectedIndex = optionCards.indexOf(selectedCard);
            String answer = optionTextViews.get(selectedIndex).getText().toString();
            userAnswers.put(question, answer);

            // Move to next question or summary
            currentQuestionIndex++;
            if (currentQuestionIndex < questionsList.size()) {
                displayQuestion(currentQuestionIndex);
            } else {
                saveAnswersToFirestore();
                currentState = State.SUMMARY;
            }
        }
        updateUiForState();
    }

    private void displayQuestion(int index) {
        // Reset card selections
        if (selectedCard != null) {
            selectedCard.setChecked(false);
            selectedCard = null;
        }

        OnboardingQuestion currentQ = questionsList.get(index);
        tvQuestion.setText(currentQ.getQuestionText());

        // Set text for each option card and manage visibility
        for (int i = 0; i < optionCards.size(); i++) {
            if (i < currentQ.getOptions().size()) {
                optionCards.get(i).setVisibility(View.VISIBLE);
                optionTextViews.get(i).setText(currentQ.getOptions().get(i));
            } else {
                optionCards.get(i).setVisibility(View.GONE); // Hide unused cards
            }
        }

        // Update progress
        progressBar.setMax(questionsList.size());
        progressBar.setProgress(index + 1, true); // Animate progress change
        tvProgressCounter.setText((index + 1) + "/" + questionsList.size());
    }

    private void onOptionCardClicked(View view) {
        if (selectedCard != null) {
            selectedCard.setChecked(false);
        }
        selectedCard = (MaterialCardView) view;
        selectedCard.setChecked(true);
    }

    private void saveAnswersToFirestore() {
        WriteBatch batch = db.batch();
        String collectionPath = "users/" + userId + "/onboardingAnswers";

        for (Map.Entry<String, String> entry : userAnswers.entrySet()) {
            Map<String, Object> answerData = new HashMap<>();
            answerData.put("question", entry.getKey());
            answerData.put("answer", entry.getValue());
            batch.set(db.collection(collectionPath).document(entry.getKey()), answerData); // Use question as doc ID
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> System.out.println("Preferences saved!"))
                .addOnFailureListener(e -> Toast.makeText(OnboardingTestActivity.this, "Error saving answers.", Toast.LENGTH_SHORT).show());
    }

    private void updateUiForState() {
        TransitionManager.beginDelayedTransition(rootLayout); // Animate layout changes

        introGroup.setVisibility(currentState == State.INTRO ? View.VISIBLE : View.GONE);
        questionGroup.setVisibility(currentState == State.QUESTION ? View.VISIBLE : View.GONE);
        summaryGroup.setVisibility(currentState == State.SUMMARY ? View.VISIBLE : View.GONE);

        boolean isQuestionState = currentState == State.QUESTION;
        progressBar.setVisibility(isQuestionState ? View.VISIBLE : View.GONE);
        tvProgressCounter.setVisibility(isQuestionState ? View.VISIBLE : View.GONE);
        btnSkip.setVisibility(isQuestionState ? View.VISIBLE : View.GONE);

        // Hide main button when summary card is shown (as it has its own button)
        btnNext.setVisibility(currentState == State.SUMMARY ? View.GONE : View.VISIBLE);

        switch (currentState) {
            case INTRO:
                btnNext.setText("Start Assessment");
                break;
            case QUESTION:
                btnNext.setText("Next");
                break;
            case SUMMARY:
                // No text change needed as btnNext is now hidden
                break;
        }
    }

    private void navigateToNextScreen() {
        // Replace with your actual next activity
        Intent intent = new Intent(OnboardingTestActivity.this, PreferenceSetupActivity.class);
        startActivity(intent);
        finish();
    }
}