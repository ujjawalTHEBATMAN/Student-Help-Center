package com.example.abcd;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.abcd.models.QuizQuestion;
import com.example.abcd.models.QuizManager;
import java.util.List;

public class QuizQuestionsActivity extends AppCompatActivity {
    private TextView questionText;
    private RadioGroup optionsGroup;
    private Button nextButton;
    private ProgressBar progressBar;
    private ProgressBar timerProgressBar;
    private TextView timerText;
    private List<QuizQuestion> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private CountDownTimer questionTimer;
    private static final int QUESTION_TIMER_SECONDS = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_questions);

        // Initialize views
        questionText = findViewById(R.id.questionText);
        optionsGroup = findViewById(R.id.optionsGroup);
        nextButton = findViewById(R.id.nextButton);
        progressBar = findViewById(R.id.progressBar);
        timerProgressBar = findViewById(R.id.timerProgressBar);
        timerText = findViewById(R.id.timerText);

        // Get the selected category
        String category = getIntent().getStringExtra("CATEGORY");

        // Get questions for the selected semester
        questions = QuizManager.getQuizQuestions(category.toLowerCase());

        if (questions.isEmpty()) {
            showError("No questions available for this semester");
            return;
        }

        // Show first question
        displayQuestion(currentQuestionIndex);

        nextButton.setOnClickListener(v -> {
            if (optionsGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check answer
            RadioButton selectedAnswer = findViewById(optionsGroup.getCheckedRadioButtonId());
            if (selectedAnswer.getText().toString().equals(questions.get(currentQuestionIndex).getCorrectAnswer())) {
                score++;
            }

            // Move to next question or finish quiz
            currentQuestionIndex++;
            if (currentQuestionIndex < questions.size()) {
                displayQuestion(currentQuestionIndex);
            } else {
                showResults();
            }
        });
    }

    private void displayQuestion(int index) {
        QuizQuestion question = questions.get(index);
        questionText.setText(question.getQuestion());
        
        optionsGroup.removeAllViews();
        for (String option : question.getOptions()) {
            RadioButton button = new RadioButton(this);
            button.setText(option);
            optionsGroup.addView(button);
        }
        
        optionsGroup.clearCheck();
        startQuestionTimer();
    }

    private void startQuestionTimer() {
        if (questionTimer != null) {
            questionTimer.cancel();
        }

        timerProgressBar.setMax(QUESTION_TIMER_SECONDS * 1000);
        timerProgressBar.setProgress(QUESTION_TIMER_SECONDS * 1000);

        questionTimer = new CountDownTimer(QUESTION_TIMER_SECONDS * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerProgressBar.setProgress((int) millisUntilFinished);
                timerText.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                // Auto-submit when timer expires
                nextButton.performClick();
            }
        }.start();
    }

    private void showResults() {
        if (questionTimer != null) {
            questionTimer.cancel();
        }
        String message = String.format("Quiz completed!\nScore: %d out of %d", score, questions.size());
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (questionTimer != null) {
            questionTimer.cancel();
        }
    }
}
