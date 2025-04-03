package com.example.abcd.adminfeature.Notification;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.abcd.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateAdminNotification extends AppCompatActivity {

    private TextInputLayout titleInputLayout;
    private EditText titleEditText;
    private TextInputLayout messageInputLayout;
    private EditText messageEditText;
    private TextInputLayout categoryInputLayout;
    private EditText categoryEditText;
    private Button sendButton;
    private ProgressBar progressBar;
    private CardView notificationCard;
    private LinearLayout rootLayout;
    private ImageView backButton;
    private TextView charCountTitle;
    private TextView charCountMessage;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_admin_notification);

        titleInputLayout = findViewById(R.id.title_input_layout);
        titleEditText = findViewById(R.id.title_edit_text);
        messageInputLayout = findViewById(R.id.message_input_layout);
        messageEditText = findViewById(R.id.message_edit_text);
        categoryInputLayout = findViewById(R.id.category_input_layout);
        categoryEditText = findViewById(R.id.category_edit_text);
        sendButton = findViewById(R.id.send_button);
        progressBar = findViewById(R.id.progress_bar);
        notificationCard = findViewById(R.id.notification_card);
        rootLayout = findViewById(R.id.root_layout);
        backButton = findViewById(R.id.back_button);
        charCountTitle = findViewById(R.id.char_count_title);
        charCountMessage = findViewById(R.id.char_count_message);

        databaseReference = FirebaseDatabase.getInstance().getReference("notifications");

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        rootLayout.setBackgroundColor(getResources().getColor(R.color.background_color));
        notificationCard.setCardBackgroundColor(getResources().getColor(R.color.card_background));
        sendButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        sendButton.setTextColor(getResources().getColor(R.color.colorOnPrimary));
        titleInputLayout.setBoxBackgroundColor(getResources().getColor(R.color.white));
        messageInputLayout.setBoxBackgroundColor(getResources().getColor(R.color.white));
        categoryInputLayout.setBoxBackgroundColor(getResources().getColor(R.color.white));
        progressBar.getIndeterminateDrawable().setColorFilter(
                getResources().getColor(R.color.colorAccent),
                android.graphics.PorterDuff.Mode.SRC_IN);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                charCountTitle.setText(s.length() + "/50");
                if (s.length() > 50) {
                    titleInputLayout.setError("Title must not exceed 50 characters");
                } else {
                    titleInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                charCountMessage.setText(s.length() + "/500");
                if (s.length() > 500) {
                    messageInputLayout.setError("Message must not exceed 500 characters");
                } else {
                    messageInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        sendButton.setOnClickListener(v -> {
            if (validateInput()) {
                sendNotification();
            }
        });
    }

    private boolean validateInput() {
        String title = titleEditText.getText().toString().trim();
        String message = messageEditText.getText().toString().trim();
        String category = categoryEditText.getText().toString().trim();

        if (title.isEmpty()) {
            titleInputLayout.setError("Title is required");
            return false;
        }
        if (message.isEmpty()) {
            messageInputLayout.setError("Message is required");
            return false;
        }
        if (category.isEmpty()) {
            categoryInputLayout.setError("Category is required");
            return false;
        }
        return true;
    }

    private void sendNotification() {
        progressBar.setVisibility(View.VISIBLE);
        sendButton.setEnabled(false);

        String title = titleEditText.getText().toString().trim();
        String message = messageEditText.getText().toString().trim();
        String category = categoryEditText.getText().toString().trim();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String notificationId = databaseReference.push().getKey();

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("category", category);
        notificationData.put("timestamp", timestamp);
        notificationData.put("status", "sent");

        databaseReference.child(notificationId).setValue(notificationData)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    sendButton.setEnabled(true);
                    Toast.makeText(this, "Notification sent successfully", Toast.LENGTH_SHORT).show();
                    animateSuccess();
                    clearInputs();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    sendButton.setEnabled(true);
                    Toast.makeText(this, "Failed to send notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void animateSuccess() {
        notificationCard.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        notificationCard.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(200)
                                .start();
                    }
                })
                .start();
    }

    private void clearInputs() {
        titleEditText.setText("");
        messageEditText.setText("");
        categoryEditText.setText("");
        charCountTitle.setText("0/50");
        charCountMessage.setText("0/500");
        hideKeyboard();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}