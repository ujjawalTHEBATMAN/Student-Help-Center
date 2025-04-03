package com.example.abcd.adminfeature.feedback;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.abcd.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FeedbackCreation extends AppCompatActivity {

    private EditText feedbackTitleEditText;
    private EditText feedbackDescriptionEditText;
    private Button submitFeedbackButton;
    private ProgressBar submissionProgressBar;
    private CardView feedbackCardView;
    private LinearLayout rootLayout;
    private TextView charCountTextView;
    private DatabaseReference feedbackRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_creation);

        feedbackTitleEditText = findViewById(R.id.feedback_title_edittext);
        feedbackDescriptionEditText = findViewById(R.id.feedback_description_edittext);
        submitFeedbackButton = findViewById(R.id.submit_feedback_button);
        submissionProgressBar = findViewById(R.id.submission_progress_bar);
        feedbackCardView = findViewById(R.id.feedback_card_view);
        rootLayout = findViewById(R.id.root_layout);
        charCountTextView = findViewById(R.id.char_count_textview);

        feedbackRef = FirebaseDatabase.getInstance().getReference("feedback");

        setupUI();
        setupListeners();
        applyAnimations();
    }

    private void setupUI() {
        GradientDrawable cardBackground = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        ContextCompat.getColor(this, R.color.gradient_start),
                        ContextCompat.getColor(this, R.color.gradient_end)
                }
        );
        cardBackground.setCornerRadius(24f);
        feedbackCardView.setBackground(cardBackground);

        submitFeedbackButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.buttonBackgroundColor));
        submitFeedbackButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        feedbackTitleEditText.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        feedbackDescriptionEditText.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        charCountTextView.setTextColor(ContextCompat.getColor(this, R.color.secondaryTextColor));
    }

    private void setupListeners() {
        feedbackDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                charCountTextView.setText(length + "/500");
                if (length > 500) {
                    charCountTextView.setTextColor(ContextCompat.getColor(FeedbackCreation.this, R.color.error));
                } else {
                    charCountTextView.setTextColor(ContextCompat.getColor(FeedbackCreation.this, R.color.secondaryTextColor));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        submitFeedbackButton.setOnClickListener(v -> {
            String title = feedbackTitleEditText.getText().toString().trim();
            String description = feedbackDescriptionEditText.getText().toString().trim();

            if (title.isEmpty() || description.isEmpty()) {
                showSnackbar("Please fill all fields");
            } else if (description.length() > 500) {
                showSnackbar("Description exceeds 500 characters");
            } else {
                submitFeedback(title, description);
            }
        });
    }

    private void applyAnimations() {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(feedbackCardView, "alpha", 0f, 1f);
        fadeIn.setDuration(1000);
        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeIn.start();

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(submitFeedbackButton, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(submitFeedbackButton, "scaleY", 0.8f, 1f);
        scaleX.setDuration(500);
        scaleY.setDuration(500);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleX.start();
        scaleY.start();
    }

    private void submitFeedback(String title, String description) {
        submissionProgressBar.setVisibility(View.VISIBLE);
        submitFeedbackButton.setEnabled(false);

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String feedbackId = feedbackRef.push().getKey();

        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("title", title);
        feedbackData.put("description", description);
        feedbackData.put("timestamp", timestamp);
        feedbackData.put("status", "pending");

        assert feedbackId != null;
        feedbackRef.child(feedbackId).setValue(feedbackData)
                .addOnSuccessListener(aVoid -> {
                    submissionProgressBar.setVisibility(View.GONE);
                    submitFeedbackButton.setEnabled(true);
                    showSuccessAnimation();
                    clearFields();
                    showSnackbar("Feedback submitted successfully!");
                })
                .addOnFailureListener(e -> {
                    submissionProgressBar.setVisibility(View.GONE);
                    submitFeedbackButton.setEnabled(true);
                    showSnackbar("Failed to submit feedback: " + e.getMessage());
                });
    }

    private void showSuccessAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(feedbackCardView, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(feedbackCardView, "scaleY", 1f, 1.1f, 1f);
        scaleX.setDuration(600);
        scaleY.setDuration(600);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleX.start();
        scaleY.start();
    }

    private void clearFields() {
        feedbackTitleEditText.setText("");
        feedbackDescriptionEditText.setText("");
        charCountTextView.setText("0/500");
    }

    private void showSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_container));
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, R.color.on_primary_container));
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}