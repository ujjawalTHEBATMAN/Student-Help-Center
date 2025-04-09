package com.example.abcd.adminfeature.feedback;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.abcd.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FeedbackCreation extends AppCompatActivity {

    private TextInputEditText feedbackTitleEditText;
    private TextInputEditText feedbackDescriptionEditText;
    private MaterialButton submitFeedbackButton;
    private ProgressBar submissionProgressBar;
    private TextInputLayout titleInputLayout;
    private TextInputLayout descriptionInputLayout;
    private View rootLayout;
    private MaterialToolbar toolbar;
    private DatabaseReference feedbackRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_creation);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        feedbackTitleEditText = findViewById(R.id.feedback_title_edittext);
        feedbackDescriptionEditText = findViewById(R.id.feedback_description_edittext);
        submitFeedbackButton = findViewById(R.id.submit_feedback_button);
        submissionProgressBar = findViewById(R.id.submission_progress_bar);
        titleInputLayout = findViewById(R.id.title_input_layout);
        descriptionInputLayout = findViewById(R.id.description_input_layout);
        rootLayout = findViewById(R.id.root_layout);

        feedbackRef = FirebaseDatabase.getInstance().getReference("feedback");

        setupListeners();
    }

    private void setupListeners() {
        feedbackDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                titleInputLayout.setError(null);
                descriptionInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        submitFeedbackButton.setOnClickListener(v -> {
            String title = feedbackTitleEditText.getText().toString().trim();
            String description = feedbackDescriptionEditText.getText().toString().trim();

            if (title.isEmpty()) {
                titleInputLayout.setError("Title cannot be empty");
                return;
            }

            if (description.isEmpty()) {
                descriptionInputLayout.setError("Description cannot be empty");
                return;
            }

            submitFeedback(title, description);
        });
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
                    showSnackbar("Feedback submitted successfully!");
                    hideKeyboard();
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    submissionProgressBar.setVisibility(View.GONE);
                    submitFeedbackButton.setEnabled(true);
                    showSnackbar("Failed to submit feedback: " + e.getMessage());
                });
    }

    private void clearFields() {
        feedbackTitleEditText.setText("");
        feedbackDescriptionEditText.setText("");
    }

    private void showSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundTintList(ContextCompat.getColorStateList(this, com.google.android.material.R.color.design_default_color_primary));
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        snackbar.show();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}