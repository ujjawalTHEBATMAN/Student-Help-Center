package com.example.abcd.ExamQuizes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.abcd.R;

import java.util.HashMap;
import java.util.Map;

public class addQuestionActivity extends AppCompatActivity {
    private CardView questionCardView;
    private EditText questionEditText;
    private EditText option1EditText;
    private EditText option2EditText;
    private EditText option3EditText;
    private EditText option4EditText;
    private Button saveQuestionButton;
    private Button nextQuestionButton;
    private Button backButton;
    private TextView questionNumberTextView;
    private LinearLayout questionNavigationLayout;
    private RadioGroup correctAnswerRadioGroup;

    // Firebase reference for the question bank node
    private DatabaseReference questionBankRef;
    private String subjectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_question);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get subject name from intent extras
        subjectName = getIntent().getStringExtra("subject_name");
        if (subjectName == null || subjectName.isEmpty()) {
            Toast.makeText(this, "Subject not specified", Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initialize Firebase reference under "Questionbank" -> subjectName
        questionBankRef = FirebaseDatabase.getInstance().getReference("Questionbank").child(subjectName);

        initializeViews();

        saveQuestionButton.setOnClickListener(v -> saveCurrentQuestion());
        // “Next Question” just clears the inputs for a new entry
        nextQuestionButton.setOnClickListener(v -> clearInputs());
        // “Finish Quiz” closes the activity
        backButton.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        questionCardView = findViewById(R.id.questionCardView);
        questionEditText = findViewById(R.id.questionEditText);
        option1EditText = findViewById(R.id.option1EditText);
        option2EditText = findViewById(R.id.option2EditText);
        option3EditText = findViewById(R.id.option3EditText);
        option4EditText = findViewById(R.id.option4EditText);
        saveQuestionButton = findViewById(R.id.saveQuestionButton);
        nextQuestionButton = findViewById(R.id.nextQuestionButton);
        backButton = findViewById(R.id.finishButton);
        questionNumberTextView = findViewById(R.id.questionNumberTextView);
        questionNavigationLayout = findViewById(R.id.questionNavigationLayout);
        correctAnswerRadioGroup = findViewById(R.id.correctAnswerRadioGroup);
    }

    private void saveCurrentQuestion() {
        if (!validateQuestionInputs()) {
            return;
        }

        int selectedAnswerIndex = getSelectedAnswerIndex();
        if (selectedAnswerIndex == -1) {
            Toast.makeText(this, "Please select the correct answer", Toast.LENGTH_SHORT).show();
            return;
        }

        String questionText = questionEditText.getText().toString().trim();
        String option1 = option1EditText.getText().toString().trim();
        String option2 = option2EditText.getText().toString().trim();
        String option3 = option3EditText.getText().toString().trim();
        String option4 = option4EditText.getText().toString().trim();

        // Create a map for the options
        Map<String, String> optionsMap = new HashMap<>();
        optionsMap.put("option1", option1);
        optionsMap.put("option2", option2);
        optionsMap.put("option3", option3);
        optionsMap.put("option4", option4);

        // Create a map for the question details
        Map<String, Object> questionData = new HashMap<>();
        questionData.put("options", optionsMap);
        questionData.put("correctAnswer", selectedAnswerIndex);

        // Save the question to Firebase under "Questionbank" -> subjectName -> questionText
        questionBankRef.child(questionText).setValue(questionData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(addQuestionActivity.this, "Question added to database!", Toast.LENGTH_SHORT).show();
                        clearInputs();
                    } else {
                        Toast.makeText(addQuestionActivity.this, "Failed to add question: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateQuestionInputs() {
        if (questionEditText.getText().toString().trim().isEmpty() ||
                option1EditText.getText().toString().trim().isEmpty() ||
                option2EditText.getText().toString().trim().isEmpty() ||
                option3EditText.getText().toString().trim().isEmpty() ||
                option4EditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Updated method to correctly determine the answer index
    private int getSelectedAnswerIndex() {
        int radioButtonId = correctAnswerRadioGroup.getCheckedRadioButtonId();
        if (radioButtonId == -1) return -1;
        if (radioButtonId == R.id.option1RadioButton) return 0;
        if (radioButtonId == R.id.option2RadioButton) return 1;
        if (radioButtonId == R.id.option3RadioButton) return 2;
        if (radioButtonId == R.id.option4RadioButton) return 3;
        return -1;
    }

    private void clearInputs() {
        questionEditText.setText("");
        option1EditText.setText("");
        option2EditText.setText("");
        option3EditText.setText("");
        option4EditText.setText("");
        correctAnswerRadioGroup.clearCheck();
    }
}
