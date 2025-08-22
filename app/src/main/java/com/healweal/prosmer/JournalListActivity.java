package com.healweal.prosmer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
public class JournalListActivity extends AppCompatActivity {

    private static final int EDITOR_REQUEST_CODE = 1001;
    private RecyclerView recyclerView;
    private JournalAdapter adapter;
    private List<JournalEntry> entryList = new ArrayList<>();
    private TextView tvEmptyState;

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerView = findViewById(R.id.journalRecyclerView);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        setupRecyclerView();

        findViewById(R.id.fabAddJournal).setOnClickListener(v -> {
            Intent intent = new Intent(JournalListActivity.this, JournalEditorActivity.class);
            startActivityForResult(intent, EDITOR_REQUEST_CODE);
        });

        fetchJournalEntries();
    }

    private void setupRecyclerView() {
        adapter = new JournalAdapter(entryList,
                entry -> { // OnItemClick
                    Intent intent = new Intent(JournalListActivity.this, JournalEditorActivity.class);
                    intent.putExtra("JOURNAL_ENTRY", entry);
                    startActivityForResult(intent, EDITOR_REQUEST_CODE);
                },
                entry -> { // OnItemLongClick
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Entry")
                            .setMessage("Are you sure you want to delete this journal entry?")
                            .setPositiveButton("Delete", (dialog, which) -> deleteEntry(entry))
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void fetchJournalEntries() {
        db.collection("users").document(userId).collection("journalEntries")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    entryList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        JournalEntry entry = doc.toObject(JournalEntry.class);
                        // Manually set ID as it's not part of the POJO by default
                        JournalEntry completeEntry = new JournalEntry(doc.getId(), entry.getTitle(), entry.getContent(), entry.getTimestamp());
                        entryList.add(completeEntry);
                    }
                    adapter.notifyDataSetChanged();
                    tvEmptyState.setVisibility(entryList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void deleteEntry(JournalEntry entry) {
        db.collection("users").document(userId).collection("journalEntries").document(entry.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show();
                    fetchJournalEntries(); // Refresh the list
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDITOR_REQUEST_CODE && resultCode == RESULT_OK) {
            // Refresh the list if an entry was saved (created or edited)
            fetchJournalEntries();
        }
    }
}