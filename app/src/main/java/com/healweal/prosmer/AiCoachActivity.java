package com.healweal.prosmer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.FinishReason;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.TextPart;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AiCoachActivity extends AppCompatActivity {

    private static final String TAG = "AiCoachActivity";

    // These variables correspond to the views in your XML layout
    private RecyclerView chatRecyclerView;
    private EditText etMessage;
    private ImageButton btnSend;
    private View typingIndicator;
    private ChipGroup suggestionChips;

    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messageList = new ArrayList<>();

    private ChatFutures chat;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private DatabaseReference messagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This links the Java class to your XML layout file
        setContentView(R.layout.activity_ai_chat);

        initViews();
        setupRecyclerView();
        setupFirebase();
        setupGemini();
        setupClickListeners();
        loadChatHistory();
    }

    private void initViews() {
        // Finding each view by its ID from the XML layout
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        typingIndicator = findViewById(R.id.typingIndicator);
        suggestionChips = findViewById(R.id.suggestionChips);
    }

    private void setupRecyclerView() {
        // Assumes you have a ChatAdapter class
        chatAdapter = new ChatAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void setupFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            messagesRef = FirebaseDatabase.getInstance().getReference("chat_sessions")
                    .child(userId)
                    .child("messages");
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupGemini() {
         String apiKey = BuildConfig.GEMINI_API_KEY;
        GenerativeModel gm = new GenerativeModel("gemini-1.5-pro-latest", apiKey);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        chat = model.startChat();
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> {
            String messageText = etMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
            }
        });

        // This correctly handles the ChipGroup from your XML
        for (int i = 0; i < suggestionChips.getChildCount(); i++) {
            Chip chip = (Chip) suggestionChips.getChildAt(i);
            chip.setOnClickListener(v -> sendMessage(chip.getText().toString()));
        }
    }

    private void loadChatHistory() {
        messagesRef.orderByChild("timestamp").limitToLast(50)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageList.clear();
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            ChatMessage msg = childSnapshot.getValue(ChatMessage.class);
                            if (msg != null) {
                                messageList.add(msg);
                            }
                        }
                        chatAdapter.notifyDataSetChanged();
                        chatRecyclerView.scrollToPosition(messageList.size() - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to load chat history.", error.toException());
                        Toast.makeText(AiCoachActivity.this, "Failed to load chat history.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Paste this entire corrected method into your AiCoachActivity.java
// Paste this entire corrected method into your AiCoachActivity.java
    private void sendMessage(String messageText) {
        // Create and display the user's message
        ChatMessage userMessage = new ChatMessage(messageText, "user", System.currentTimeMillis());
        addMessageAndSave(userMessage);
        etMessage.setText("");

        // Show that the AI is working
        typingIndicator.setVisibility(View.VISIBLE);
        suggestionChips.setVisibility(View.GONE);

        // Create the message content for the API
        Content.Builder builder = new Content.Builder();
        builder.setRole("user");
        builder.addPart(new TextPart(messageText));
        Content userContent = builder.build();

        // Call the Gemini API
        ListenableFuture<GenerateContentResponse> response = chat.sendMessage(userContent);

        // Handle the API's response
        Futures.addCallback(response, new FutureCallback<>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    // Check if the response was blocked for safety reasons
                    if (!result.getCandidates().isEmpty() && result.getCandidates().get(0).getFinishReason() == FinishReason.SAFETY) {
                        handleBlockedResponse("The response was blocked for safety reasons.");
                        return;
                    }

                    // Get the AI's text response and display it
                    String aiResponseText = result.getText();
                    ChatMessage aiMessage = new ChatMessage(aiResponseText, "model", System.currentTimeMillis());
                    runOnUiThread(() -> addMessageAndSave(aiMessage));

                } catch (Exception e) {
                    Log.e(TAG, "Error processing Gemini response", e);
                    handleBlockedResponse("Received an invalid response from the AI.");
                } finally {
                    // Always reset the UI
                    runOnUiThread(() -> {
                        typingIndicator.setVisibility(View.GONE);
                        suggestionChips.setVisibility(View.VISIBLE);
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                Log.e(TAG, "Error getting response from API", t);
                runOnUiThread(() -> {
                    typingIndicator.setVisibility(View.GONE);
                    suggestionChips.setVisibility(View.VISIBLE);
                    Toast.makeText(AiCoachActivity.this, "Error: Could not connect to AI.", Toast.LENGTH_LONG).show();
                });
            }
        }, executor);
    }


    private void addMessageAndSave(ChatMessage message) {
        messageList.add(message);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        chatRecyclerView.scrollToPosition(messageList.size() - 1);

        messagesRef.push().setValue(message)
                .addOnFailureListener(e -> Log.w(TAG, "Error adding message to Realtime DB", e));
    }

    private void handleBlockedResponse(String message) {
        ChatMessage blockedMessage = new ChatMessage(message, "model", System.currentTimeMillis());
        runOnUiThread(() -> {
            addMessageAndSave(blockedMessage);
            typingIndicator.setVisibility(View.GONE);
            suggestionChips.setVisibility(View.VISIBLE);
        });
    }
}