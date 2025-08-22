package com.healweal.prosmer; // Use your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
// --- CHANGED: Imports for Realtime Database ---
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreferenceSetupActivity extends AppCompatActivity {

    // UI Elements
    private MaterialCardView cardGoalHealth, cardGoalProductivity, cardGoalMindfulness, cardGoalLearning;
    private LinearLayout habitsContainer;
    private ChipGroup chipGroupInterests;
    private Button btnSavePreferences;

    // --- CHANGED: Firebase Realtime Database reference ---
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference_setup);

        // --- CHANGED: Init Firebase Realtime Database ---
        // Get the root reference of your database
        databaseReference = FirebaseDatabase.getInstance().getReference();
        // Ensure you have a current user
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            // Handle user not being logged in (e.g., redirect to login)
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            // Optional: Redirect to login activity
            // startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        cardGoalHealth = findViewById(R.id.cardGoalHealth);
        cardGoalProductivity = findViewById(R.id.cardGoalProductivity);
        cardGoalMindfulness = findViewById(R.id.cardGoalMindfulness);
        cardGoalLearning = findViewById(R.id.cardGoalLearning);

        habitsContainer = findViewById(R.id.habitsContainer);
        chipGroupInterests = findViewById(R.id.chipGroupInterests);
        btnSavePreferences = findViewById(R.id.btnSavePreferences);
    }

    private void setupClickListeners() {
        // Simple toggle for the goal cards
        cardGoalHealth.setOnClickListener(v -> cardGoalHealth.setChecked(!cardGoalHealth.isChecked()));
        cardGoalProductivity.setOnClickListener(v -> cardGoalProductivity.setChecked(!cardGoalProductivity.isChecked()));
        cardGoalMindfulness.setOnClickListener(v -> cardGoalMindfulness.setChecked(!cardGoalMindfulness.isChecked()));
        cardGoalLearning.setOnClickListener(v -> cardGoalLearning.setChecked(!cardGoalLearning.isChecked()));

        btnSavePreferences.setOnClickListener(v -> saveAllPreferences());
    }

    // --- METHOD UPDATED to use Realtime Database ---
    private void saveAllPreferences() {
        List<String> selectedGoals = getSelectedGoals();
        List<String> selectedHabits = getSelectedHabits();
        List<String> selectedInterests = getSelectedInterests();

        if (selectedGoals.isEmpty() || selectedHabits.isEmpty()) {
            Toast.makeText(this, "Please select at least one goal and habit.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent multiple clicks
        btnSavePreferences.setEnabled(false);
        btnSavePreferences.setText("Saving...");

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("goals", selectedGoals);
        preferences.put("habits", selectedHabits);
        preferences.put("interests", selectedInterests);

        // Save data to Realtime Database under the user's ID
        // The structure will be: root -> users -> {userId} -> {preferences_data}
        databaseReference.child("users").child(userId)
                .updateChildren(preferences) // Use updateChildren to add/update fields without overwriting
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showSuccessAndNavigate();
                    } else {
                        Toast.makeText(this, "Error saving preferences. Please try again.", Toast.LENGTH_LONG).show();
                        // Re-enable button on failure
                        btnSavePreferences.setEnabled(true);
                        btnSavePreferences.setText("Save Preferences");
                    }
                });
    }

    private List<String> getSelectedGoals() {
        List<String> goals = new ArrayList<>();
        if (cardGoalHealth.isChecked()) goals.add("Health");
        if (cardGoalProductivity.isChecked()) goals.add("Productivity");
        if (cardGoalMindfulness.isChecked()) goals.add("Mindfulness");
        if (cardGoalLearning.isChecked()) goals.add("Learning");
        return goals;
    }

    private List<String> getSelectedHabits() {
        List<String> habits = new ArrayList<>();
        for (int i = 0; i < habitsContainer.getChildCount(); i++) {
            if (habitsContainer.getChildAt(i) instanceof CheckBox) {
                CheckBox cb = (CheckBox) habitsContainer.getChildAt(i);
                if (cb.isChecked()) {
                    habits.add(cb.getText().toString());
                }
            }
        }
        return habits;
    }

    private List<String> getSelectedInterests() {
        List<String> interests = new ArrayList<>();
        for (int id : chipGroupInterests.getCheckedChipIds()) {
            Chip chip = chipGroupInterests.findViewById(id);
            interests.add(chip.getText().toString());
        }
        return interests;
    }

    private void showSuccessAndNavigate() {
        Snackbar.make(findViewById(android.R.id.content), "Preferences saved 🎯", Snackbar.LENGTH_LONG).show();

        // Use a Handler to delay navigation so the user can see the snackbar
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(PreferenceSetupActivity.this, MoodEntryActivity.class);
            // Clear the back stack so user can't go back to onboarding
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 1500); // 1.5 second delay
    }
}