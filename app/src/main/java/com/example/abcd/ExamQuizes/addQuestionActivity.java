package com.example.abcd.ExamQuizes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.abcd.R;

import java.util.ArrayList;

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
    private int currentQuestionNumber = 1;
    private int totalQuestions = 0;
    private ArrayList<QuizQuestion> savedQuestions;

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

        savedQuestions = new ArrayList<>();
        initializeViews();

        saveQuestionButton.setOnClickListener(v -> saveCurrentQuestion());
        nextQuestionButton.setOnClickListener(v -> moveToNextQuestion());
        backButton.setOnClickListener(v -> finishWithResult());
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
        
        updateQuestionNumberDisplay();
        setupQuestionNavigation();
    }

    private void saveCurrentQuestion() {
        if (validateQuestionInputs()) {
            int selectedAnswerIndex = getSelectedAnswerIndex();
            if (selectedAnswerIndex == -1) {
                Toast.makeText(this, "Please select the correct answer", Toast.LENGTH_SHORT).show();
                return;
            }

            QuizQuestion question = new QuizQuestion(
                questionEditText.getText().toString(),
                option1EditText.getText().toString(),
                option2EditText.getText().toString(),
                option3EditText.getText().toString(),
                option4EditText.getText().toString(),
                selectedAnswerIndex
            );
            
            if (currentQuestionNumber <= savedQuestions.size()) {
                savedQuestions.set(currentQuestionNumber - 1, question);
            } else {
                savedQuestions.add(question);
            }
            
            Toast.makeText(this, "Question saved!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateQuestionNumberDisplay() {
        questionNumberTextView.setText("Question " + currentQuestionNumber);
    }

    private void setupQuestionNavigation() {
        if (questionNavigationLayout == null) return;
        questionNavigationLayout.removeAllViews();
        if (savedQuestions == null) savedQuestions = new ArrayList<>();
        for (int i = 0; i < Math.max(savedQuestions.size(), currentQuestionNumber); i++) {
            MaterialButton questionButton = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            questionButton.setText(String.valueOf(i + 1));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            questionButton.setLayoutParams(params);
            
            // Highlight current question
            if (i + 1 == currentQuestionNumber) {
                questionButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.error)));
                questionButton.setTextColor(Color.WHITE);
            }

            final int questionIndex = i;
            questionButton.setOnClickListener(v -> loadQuestion(questionIndex + 1));

            questionNavigationLayout.addView(questionButton);
        }
    }

    private void loadQuestion(int questionNumber) {
        saveCurrentQuestion(); // Save current question before loading new one
        currentQuestionNumber = questionNumber;
        updateQuestionNumberDisplay();

        if (questionNumber <= savedQuestions.size()) {
            QuizQuestion question = savedQuestions.get(questionNumber - 1);
            questionEditText.setText(question.getQuestion());
            option1EditText.setText(question.getOption1());
            option2EditText.setText(question.getOption2());
            option3EditText.setText(question.getOption3());
            option4EditText.setText(question.getOption4());
            
            // Set the correct answer radio button
            if (question.getCorrectAnswerIndex() >= 0) {
                ((RadioButton) correctAnswerRadioGroup.getChildAt(question.getCorrectAnswerIndex())).setChecked(true);
            }
        } else {
            clearInputs();
        }
    }

    private void clearInputs() {
        questionEditText.setText("");
        option1EditText.setText("");
        option2EditText.setText("");
        option3EditText.setText("");
        option4EditText.setText("");
        correctAnswerRadioGroup.clearCheck();
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

    private int getSelectedAnswerIndex() {
        int radioButtonId = correctAnswerRadioGroup.getCheckedRadioButtonId();
        if (radioButtonId == -1) return -1;
        View radioButton = correctAnswerRadioGroup.findViewById(radioButtonId);
        return correctAnswerRadioGroup.indexOfChild(radioButton);
    }

    private void moveToNextQuestion() {
        saveCurrentQuestion();
        currentQuestionNumber++;
        updateQuestionNumberDisplay();
        setupQuestionNavigation();
        clearInputs();
    }

    // Modify finishWithResult
    private void finishWithResult() {
        Intent resultIntent = new Intent();
        resultIntent.putParcelableArrayListExtra("questions", savedQuestions);
        resultIntent.putExtra("savedQuestionsCount", savedQuestions.size());
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}