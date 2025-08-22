package com.healweal.prosmer; // Use your package name

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements HabitAdapter.OnHabitToggleListener {

    private TextView tvGreeting, tvStreak, tvPromptTitle, tvJournalSnippet;
    private RecyclerView habitsRecyclerView;
    private HabitAdapter habitAdapter;
    private List<Habit> habitList = new ArrayList<>();
    private ImageView moodHappy, moodNeutral, moodSad;

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadAllDashboardData();
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvStreak = findViewById(R.id.tvStreak);
        tvPromptTitle = findViewById(R.id.tvPromptTitle);
        tvJournalSnippet = findViewById(R.id.tvJournalSnippet);
        habitsRecyclerView = findViewById(R.id.habitsRecyclerView);
        moodHappy = findViewById(R.id.moodHappy);
        moodNeutral = findViewById(R.id.moodNeutral);
        moodSad = findViewById(R.id.moodSad);
    }

    private void setupRecyclerView() {
        habitAdapter = new HabitAdapter(habitList, this);
        habitsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        habitsRecyclerView.setAdapter(habitAdapter);
        // Disable scrolling for the RecyclerView as it's inside a ScrollView
        habitsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        findViewById(R.id.cardDailyPrompt).setOnClickListener(v -> {
            startActivity(new Intent(this, DailyPromptDetailActivity.class));
        });

        View.OnClickListener moodClickListener = v -> {
            String mood = v.getTag().toString();
            saveMood(mood);
            updateMoodSelection(mood);
        };
        moodHappy.setOnClickListener(moodClickListener);
        moodNeutral.setOnClickListener(moodClickListener);
        moodSad.setOnClickListener(moodClickListener);

        // Add listeners for [+ Add Journal] and [+ Add Habit] buttons if needed
    }

    private void loadAllDashboardData() {
        // Fetch User Data (Name, Streak)
        db.collection("users").document(userId).get().addOnSuccessListener(this::updateUserData);

        // Fetch Habits
        db.collection("users").document(userId).collection("habits").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    habitList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        // For a real app, you'd check a "lastCompletedDate" field
                        // For this MVP, we assume isCompleted is for today
                        boolean isCompleted = doc.contains("isCompletedToday") && doc.getBoolean("isCompletedToday");
                        habitList.add(new Habit(doc.getId(), doc.getString("name"), isCompleted));
                    }
                    habitAdapter.notifyDataSetChanged();
                });

        // Fetch Last Journal Entry
        db.collection("users").document(userId).collection("journalEntries")
                .orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String content = queryDocumentSnapshots.getDocuments().get(0).getString("content");
                        tvJournalSnippet.setText(content.substring(0, Math.min(content.length(), 120)) + "...");
                    } else {
                        tvJournalSnippet.setText("Write your first journal entry to see a preview here.");
                    }
                });

        // (You would also fetch today's mood and daily prompt here)
    }

    private void updateUserData(DocumentSnapshot document) {
        if (document.exists()) {
            String name = document.getString("name");
            updateGreeting(name);

            long streak = document.contains("streakCount") ? document.getLong("streakCount") : 0;
            tvStreak.setText(String.format(Locale.getDefault(), "%d Days", streak));
        }
    }

    private void updateGreeting(String name) {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        String greeting;

        if (timeOfDay >= 0 && timeOfDay < 12) {
            greeting = "Good Morning";
        } else if (timeOfDay >= 12 && timeOfDay < 18) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }
        tvGreeting.setText(String.format("%s, %s 👋", greeting, name));
    }

    private void saveMood(String mood) {
        // In a real app, you'd save this to a "moodEntries" subcollection with a timestamp
        Toast.makeText(this, mood + " mood saved for today!", Toast.LENGTH_SHORT).show();
    }

    private void updateMoodSelection(String mood) {
        moodHappy.setAlpha(mood.equals("Happy") ? 1.0f : 0.5f);
        moodNeutral.setAlpha(mood.equals("Neutral") ? 1.0f : 0.5f);
        moodSad.setAlpha(mood.equals("Sad") ? 1.0f : 0.5f);
    }

    @Override
    public void onHabitToggled(String habitId, boolean isCompleted) {
        // Update the habit in Firestore
        db.collection("users").document(userId).collection("habits").document(habitId)
                .update("isCompletedToday", isCompleted)
                .addOnSuccessListener(aVoid -> {
                    // You might want to update the streak counter here after a successful update
                });
    }
}