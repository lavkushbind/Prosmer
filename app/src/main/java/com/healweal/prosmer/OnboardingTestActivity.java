package com.healweal.prosmer;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;

public class OnboardingTestActivity extends AppCompatActivity {

    private RadioGroup moodRadioGroup;
    private Slider energySlider;
    private ChipGroup goalsChipGroup;
    private MaterialButton btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_test);

        // Initialize UI components
        moodRadioGroup = findViewById(R.id.moodRadioGroup);
        energySlider = findViewById(R.id.energySlider);
        goalsChipGroup = findViewById(R.id.goalsChipGroup);
        btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectAndProcessData();
            }
        });
    }

    private void collectAndProcessData() {
         int selectedMoodId = moodRadioGroup.getCheckedRadioButtonId();
        if (selectedMoodId == -1) {
            Toast.makeText(this, "Please select your current mood", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedMoodButton = findViewById(selectedMoodId);
        String mood = selectedMoodButton.getText().toString().split("\n")[1]; // Gets "Happy", "Okay", or "Sad"

        int energyLevel = (int) energySlider.getValue();

        List<String> selectedGoals = new ArrayList<>();
        List<Integer> checkedChipIds = goalsChipGroup.getCheckedChipIds();

        if (checkedChipIds.isEmpty()) {
            Toast.makeText(this, "Please select at least one goal", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Integer id : checkedChipIds) {
            Chip chip = goalsChipGroup.findViewById(id);
            selectedGoals.add(chip.getText().toString());
        }


        String summary = "Mood: " + mood + "\nEnergy: " + energyLevel + "%\nGoals: " + selectedGoals.toString();
        Toast.makeText(this, summary, Toast.LENGTH_LONG).show();

    }
}