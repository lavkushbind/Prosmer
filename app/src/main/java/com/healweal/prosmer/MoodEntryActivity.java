package com.healweal.prosmer; // Use your package name

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MoodEntryActivity extends AppCompatActivity {

    private static final String TAG = "MoodEntryActivity";

    // UI Views
    private ImageView moodHappy, moodNeutral, moodSad;
    private TextView tvIntensityLabel;
    private SeekBar seekBarIntensity;
    private ChipGroup chipGroupTags;
    private Button btnSaveMood;
    private LinearLayout moodChartContainer;
    private final List<View> moodBars = new ArrayList<>();

    // State & Firebase
    private String selectedMood = "";
    private DatabaseReference userMoodsRef; // Reference for Realtime Database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_entry);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Security check: Ensure user is logged in
        if (currentUser == null) {
            Toast.makeText(this, "Authentication failed. Please log in.", Toast.LENGTH_LONG).show();
            // Redirect to your LoginActivity here
            finish();
            return;
        }
        String userId = currentUser.getUid();

        // Initialize Realtime Database reference
        userMoodsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("moodEntries");

        initViews();
        setupListeners();

        // Load chart data after the UI has been drawn to get correct container height
        moodChartContainer.post(this::loadWeeklyMoodData);
    }

    private void initViews() {
        moodHappy = findViewById(R.id.moodHappy);
        moodNeutral = findViewById(R.id.moodNeutral);
        moodSad = findViewById(R.id.moodSad);
        tvIntensityLabel = findViewById(R.id.tvIntensityLabel);
        seekBarIntensity = findViewById(R.id.seekBarIntensity);
        chipGroupTags = findViewById(R.id.chipGroupTags);
        btnSaveMood = findViewById(R.id.btnSaveMood);
        moodChartContainer = findViewById(R.id.moodChartContainer);

        moodBars.clear();
        for (int i = 0; i < moodChartContainer.getChildCount(); i++) {
            moodBars.add(moodChartContainer.getChildAt(i));
        }
    }

    private void setupListeners() {
        View.OnClickListener moodClickListener = v -> {
            selectedMood = v.getTag().toString();
            updateMoodSelection(selectedMood);
        };
        moodHappy.setOnClickListener(moodClickListener);
        moodNeutral.setOnClickListener(moodClickListener);
        moodSad.setOnClickListener(moodClickListener);

        seekBarIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvIntensityLabel.setText(String.format(Locale.getDefault(), "Intensity: %d", progress + 1));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        tvIntensityLabel.setText(String.format(Locale.getDefault(), "Intensity: %d", seekBarIntensity.getProgress() + 1));
        btnSaveMood.setOnClickListener(v -> saveMood());
    }

    private void updateMoodSelection(String mood) {
        moodHappy.setAlpha(mood.equals("Happy") ? 1.0f : 0.4f);
        moodNeutral.setAlpha(mood.equals("Neutral") ? 1.0f : 0.4f);
        moodSad.setAlpha(mood.equals("Sad") ? 1.0f : 0.4f);
    }

    private void saveMood() {
        if (selectedMood.isEmpty()) {
            Toast.makeText(this, "Please select your mood", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveMood.setEnabled(false); // Prevent multiple clicks

        int intensity = seekBarIntensity.getProgress() + 1;
        List<String> tags = new ArrayList<>();
        for (int id : chipGroupTags.getCheckedChipIds()) {
            Chip chip = chipGroupTags.findViewById(id);
            if (chip != null) {
                tags.add(chip.getText().toString());
            }
        }

        // Use a Map to include the ServerValue.TIMESTAMP
        Map<String, Object> moodEntryMap = new HashMap<>();
        moodEntryMap.put("mood", selectedMood);
        moodEntryMap.put("intensity", intensity);
        moodEntryMap.put("tags", tags);
        moodEntryMap.put("timestamp", ServerValue.TIMESTAMP); // Let Firebase server set the time

        // push() generates a unique key for each new entry
        userMoodsRef.push().setValue(moodEntryMap, (error, ref) -> {
            if (error == null) {
                Toast.makeText(this, "Mood saved ✔", Toast.LENGTH_SHORT).show();
                showSuccessAndNavigate();
            } else {
                Toast.makeText(this, "Error saving mood: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error saving mood", error.toException());
                btnSaveMood.setEnabled(true); // Re-enable button on failure
            }
        });
    }

    private void showSuccessAndNavigate() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(MoodEntryActivity.this, AiCoachActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 1500); // 1.5 second delay
    }

    private void loadWeeklyMoodData() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6); // Get data for the last 7 days (including today)
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        long sevenDaysAgoTimestamp = cal.getTimeInMillis();

        // Query the database for entries from the last 7 days
        userMoodsRef.orderByChild("timestamp").startAt(sevenDaysAgoTimestamp)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<MoodEntry> moodEntries = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            MoodEntry entry = snapshot.getValue(MoodEntry.class);
                            if (entry != null) {
                                moodEntries.add(entry);
                            }
                        }
                        populateMoodChart(moodEntries);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Error loading weekly mood data", databaseError.toException());
                    }
                });
    }

    private void populateMoodChart(List<MoodEntry> moodEntries) {
        if (moodBars.size() != 7) {
            Log.e(TAG, "Mood chart must have exactly 7 bars defined in XML.");
            return;
        }

        // Map to hold the highest intensity mood for each day of the week
        Map<Integer, Integer> dailyMaxIntensity = new HashMap<>();

        for (MoodEntry entry : moodEntries) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(entry.timestamp);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // Sunday=1, ..., Saturday=7

            // If we already have an entry for this day, only keep the one with the highest intensity
            if (!dailyMaxIntensity.containsKey(dayOfWeek) || entry.intensity > dailyMaxIntensity.get(dayOfWeek)) {
                dailyMaxIntensity.put(dayOfWeek, entry.intensity);
            }
        }

        // Update the UI bars
        Calendar todayCal = Calendar.getInstance();
        int todayOfWeek = todayCal.get(Calendar.DAY_OF_WEEK);

        for (int i = 0; i < 7; i++) {
            // Calculate which day of the week this bar represents, going backwards from today
            // i=6 is today, i=5 is yesterday, and so on.
            int dayIndex = (todayOfWeek - (6 - i) - 1 + 7) % 7 + 1;
            int intensity = dailyMaxIntensity.getOrDefault(dayIndex, 0);

            View bar = moodBars.get(i);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bar.getLayoutParams();
            int containerHeight = moodChartContainer.getHeight();

            if (containerHeight > 0) {
                // Map intensity (1-5) to a percentage of the container's height
                params.height = (intensity == 0) ? 0 : (int) (containerHeight * (intensity / 5.0));
                // You can set a minimum height so even low intensity is visible
                if (params.height > 0 && params.height < 10) params.height = 10;
                bar.setLayoutParams(params);
            }
        }
    }
}