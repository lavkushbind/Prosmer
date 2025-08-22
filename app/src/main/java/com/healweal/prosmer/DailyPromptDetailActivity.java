package com.healweal.prosmer; // Use your package name

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
// --- CHANGED: Import for Realtime Database ---
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class DailyPromptDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvPromptTitle;
    private EditText etJournalEntry;
    private Button btnSaveEntry;
    private MaterialCardView successCard;
    private View writingGroup;

    // --- CHANGED: Use DatabaseReference for Realtime Database ---
    private DatabaseReference userJournalRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_prompt_detail);

        // --- CHANGED: Get instance and reference for Realtime Database ---
        // The path will be /users/{userId}/journalEntries/
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userJournalRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("journalEntries");

        initViews();
        setupListeners();

        // You would get the prompt title from the Intent passed by HomeActivity
        // String prompt = getIntent().getStringExtra("PROMPT_TITLE");
        // tvPromptTitle.setText(prompt);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvPromptTitle = findViewById(R.id.tvPromptTitle);
        etJournalEntry = findViewById(R.id.etJournalEntry);
        btnSaveEntry = findViewById(R.id.btnSaveEntry);
        successCard = findViewById(R.id.successCard);
        writingGroup = findViewById(R.id.writingGroup);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnSaveEntry.setOnClickListener(v -> saveJournalEntry());
    }

    private void saveJournalEntry() {
        String entryText = etJournalEntry.getText().toString().trim();
        String promptTitle = tvPromptTitle.getText().toString();

        if (entryText.isEmpty()) {
            Toast.makeText(this, "Please write something before saving.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent multiple saves
        btnSaveEntry.setEnabled(false);
        btnSaveEntry.setText("Saving...");

        Map<String, Object> journalEntry = new HashMap<>();
        journalEntry.put("prompt", promptTitle);
        journalEntry.put("content", entryText);
        // --- CHANGED: Use ServerValue.TIMESTAMP for Realtime Database ---
        journalEntry.put("timestamp", ServerValue.TIMESTAMP);

        // --- CHANGED: Logic to save to Realtime Database ---
        // push() creates a new unique ID for the journal entry
        userJournalRef.push().setValue(journalEntry)
                .addOnSuccessListener(aVoid -> {
                    // The 'aVoid' parameter is not used but is required by the listener
                    showSuccessAndFinish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving entry. Please try again.", Toast.LENGTH_LONG).show();
                    // Re-enable button on failure
                    btnSaveEntry.setEnabled(true);
                    btnSaveEntry.setText("Save Entry");
                });
    }

    private void showSuccessAndFinish() {
        // Hide the keyboard and writing UI
        writingGroup.setVisibility(View.GONE);
        btnSaveEntry.setVisibility(View.GONE);
        btnBack.setVisibility(View.GONE);

        // Show the success card with a fade-in animation
        successCard.setAlpha(0f);
        successCard.setVisibility(View.VISIBLE);
        successCard.animate().alpha(1f).setDuration(500).start();

        // Wait for 2 seconds, then go back to the home screen
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            finish(); // Closes this activity and returns to the previous one (Home)
        }, 2000);
    }
}