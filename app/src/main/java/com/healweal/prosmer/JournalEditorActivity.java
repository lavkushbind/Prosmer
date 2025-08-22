package com.healweal.prosmer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;


public class JournalEditorActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private Button btnSave;

    private FirebaseFirestore db;
    private String userId;
    private JournalEntry currentEntry; // Holds the entry being edited

    private SharedPreferences prefs;
    private static final String DRAFT_TITLE = "draft_title";
    private static final String DRAFT_CONTENT = "draft_content";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_editor);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences("JournalDraft", MODE_PRIVATE);

        etTitle = findViewById(R.id.etJournalTitle);
        etContent = findViewById(R.id.etJournalContent);
        btnSave = findViewById(R.id.btnSaveJournal);

        Toolbar toolbar = findViewById(R.id.toolbarEditor);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (getIntent().hasExtra("JOURNAL_ENTRY")) {
            currentEntry = getIntent().getParcelableExtra("JOURNAL_ENTRY");
            etTitle.setText(currentEntry.getTitle());
            etContent.setText(currentEntry.getContent());
        } else {
            // Load draft for new entries only
            loadDraft();
        }

        btnSave.setOnClickListener(v -> saveJournalEntry());
    }

    private void saveJournalEntry() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(this, "Journal content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("content", content);
        data.put("timestamp", FieldValue.serverTimestamp());

        Task<Void> saveTask;
        if (currentEntry != null) { // Editing existing entry
            saveTask = db.collection("users").document(userId).collection("journalEntries").document(currentEntry.getId()).set(data, SetOptions.merge());
        } else { // Creating new entry
            saveTask = db.collection("users").document(userId).collection("journalEntries").add(data).continueWith(task -> null);
        }

        saveTask.addOnSuccessListener(aVoid -> {
            clearDraft();
            Snackbar.make(findViewById(android.R.id.content), "Journal saved ✔", Snackbar.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        }).addOnFailureListener(e -> Toast.makeText(this, "Error saving journal", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save draft only if it's a new entry
        if (currentEntry == null) {
            saveDraft();
        }
    }

    private void saveDraft() {
        prefs.edit()
                .putString(DRAFT_TITLE, etTitle.getText().toString())
                .putString(DRAFT_CONTENT, etContent.getText().toString())
                .apply();
    }

    private void loadDraft() {
        etTitle.setText(prefs.getString(DRAFT_TITLE, ""));
        etContent.setText(prefs.getString(DRAFT_CONTENT, ""));
    }

    private void clearDraft() {
        prefs.edit().clear().apply();
    }
}