package com.example.abcd.ExamQuizes;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.abcd.ExamQuizes.Quiz;
import com.example.abcd.ExamQuizes.QuizQuestion;
import com.example.abcd.ExamQuizes.QuizResult;
import com.example.abcd.ExamQuizes.QuizResultsActivity;
import com.example.abcd.ExamQuizes.examQuizesMainActivity;
import com.example.abcd.R;
import com.example.abcd.databinding.ActivityQuizBinding;
import com.example.abcd.utils.SessionManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import android.util.Log;

public class QuizActivity extends AppCompatActivity {

    private ActivityQuizBinding binding;
    private Quiz currentQuiz;
    private CountDownTimer quizTimer;
    private int currentQuestionIndex = 0;
    private List<Integer> selectedAnswers = new ArrayList<>();
    private static final long TIMER_UPDATE_INTERVAL = 1000L;
    private SessionManager sessionManager;
    private DatabaseReference resultsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase and Session
        sessionManager = new SessionManager(this);
        resultsRef = FirebaseDatabase.getInstance().getReference("quiz_results");

        initializeQuiz();
        setupTimer();
        setupQuestionNavigation();
        showCurrentQuestion();
    }

    private void initializeQuiz() {
        currentQuiz = getIntent().getParcelableExtra("SELECTED_QUIZ");
        if (currentQuiz == null) {
            Toast.makeText(this, "Error loading quiz", Toast.LENGTH_SHORT).show();
            finish();
        }
        initializeSelectedAnswers();
        setupProgressBar();
    }

    private void initializeSelectedAnswers() {
        for (int i = 0; i < currentQuiz.getQuestions().size(); i++) {
            selectedAnswers.add(-1); // -1 indicates unanswered
        }
    }

    private void setupProgressBar() {
        binding.progressBar.setMax(currentQuiz.getQuestions().size());
    }

    private void setupTimer() {
        long remainingTime = currentQuiz.getEndingTime() - System.currentTimeMillis();

        quizTimer = new CountDownTimer(remainingTime, TIMER_UPDATE_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTimerUI(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                handleQuizExpiration();
            }
        }.start();
    }

    private void updateTimerUI(long millisUntilFinished) {
        String timerText = String.format(Locale.getDefault(),
                "Time Remaining: %02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60);

        binding.tvTimer.setText(timerText);
    }

    private void showCurrentQuestion() {
        QuizQuestion question = currentQuiz.getQuestions().get(currentQuestionIndex);

        // Update question text
        binding.tvQuestion.setText(question.getQuestion());

        // Update progress
        binding.tvQuestionProgress.setText(String.format(Locale.getDefault(),
                "Question %d/%d",
                currentQuestionIndex + 1,
                currentQuiz.getQuestions().size()));

        binding.progressBar.setProgress(currentQuestionIndex + 1);

        // Clear previous options
        binding.rgOptions.removeAllViews();

        // Add new options
        List<String> options = Arrays.asList(
                question.getOption1(),
                question.getOption2(),
                question.getOption3(),
                question.getOption4()
        );

        for (int i = 0; i < options.size(); i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(options.get(i));
            radioButton.setId(View.generateViewId());
            radioButton.setTextSize(16);
            radioButton.setPadding(16, 16, 16, 16);

            // Set checked state if previously answered
            if (selectedAnswers.get(currentQuestionIndex) == i) {
                radioButton.setChecked(true);
            }

            final int finalIndex = i;
            radioButton.setOnClickListener(v -> {
                selectedAnswers.set(currentQuestionIndex, finalIndex);
                updateNavigationButtons();
            });

            binding.rgOptions.addView(radioButton);
        }

        updateNavigationButtons();
    }

    private void setupQuestionNavigation() {
        binding.btnPrevious.setOnClickListener(v -> {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                showCurrentQuestion();
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            if (currentQuestionIndex < currentQuiz.getQuestions().size() - 1) {
                currentQuestionIndex++;
                showCurrentQuestion();
            }
        });

        binding.btnSubmit.setOnClickListener(v -> submitQuiz());
    }

    private void updateNavigationButtons() {
        // Previous button
        binding.btnPrevious.setEnabled(currentQuestionIndex > 0);

        // Next/Submit button
        if (currentQuestionIndex == currentQuiz.getQuestions().size() - 1) {
            binding.btnNext.setVisibility(View.GONE);
            binding.btnSubmit.setVisibility(View.VISIBLE);
        } else {
            binding.btnNext.setVisibility(View.VISIBLE);
            binding.btnSubmit.setVisibility(View.GONE);
        }
    }

    private void handleQuizExpiration() {
        quizTimer.cancel();
        Toast.makeText(this, "Time's up! Submitting quiz...", Toast.LENGTH_SHORT).show();
        submitQuiz();
    }

    private void submitQuiz() {
        quizTimer.cancel();
        QuizResult result = calculateScore();
        saveResultToFirebase(result); // Navigation happens inside saveResultToFirebase
    }

    private QuizResult calculateScore() {
        float totalMarks = 0;
        List<QuizQuestion> questions = currentQuiz.getQuestions();
        float maxMarks = questions.size() * currentQuiz.getMarksPerQuestion();

        Log.d("Scoring", "Total questions: " + questions.size());

        for (int i = 0; i < questions.size(); i++) {
            int selected = selectedAnswers.get(i);
            int correctIndex = questions.get(i).getCorrectAnswerIndex();

            Log.d("Scoring", "Question " + (i+1) + ": Selected=" + selected + ", Correct=" + correctIndex);

            if (selected == correctIndex) {
                totalMarks += currentQuiz.getMarksPerQuestion();
            } else if (selected != -1) {
                totalMarks -= currentQuiz.getNegativeMarking();
            }
        }

        Log.d("Scoring", "Raw score: " + totalMarks);
        totalMarks = Math.max(totalMarks, 0);
        Log.d("Scoring", "Final score: " + totalMarks);

        return new QuizResult(
                sessionManager.getEmail(),
                totalMarks,
                maxMarks,
                System.currentTimeMillis()
        );
    }

    private void saveResultToFirebase(QuizResult result) {
        String resultId = resultsRef.push().getKey();
        if (resultId != null) {
            resultsRef.child(resultId).setValue(result)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("QuizActivity", "Result saved successfully");
                        navigateToResults(result); // Move navigation here
                    })
                    .addOnFailureListener(e -> {
                        Log.e("QuizActivity", "Error saving result", e);
                        Toast.makeText(this, "Failed to save result", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void navigateToResults(QuizResult result) {
        if (currentQuiz.getResultViewType().equals("Advance Result View")) {
            // Show leaderboard
            Intent intent = new Intent(this, QuizResultsActivity.class);
            intent.putExtra("quiz_result", result);
            startActivity(intent);
            finish();
        } else {
            // Send email and return to main activity
            if (sendResultEmail(result)) {
                // Only finish after email client is launched
                finish();
            } else {
                // Handle email failure
                Toast.makeText(this, "Returning to quizzes", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, examQuizesMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    }

    private boolean sendResultEmail(QuizResult result) {
        try {
            String email = sessionManager.getEmail();
            if (email == null || email.isEmpty()) {
                Toast.makeText(this, "No registered email found", Toast.LENGTH_SHORT).show();
                return false;
            }

            // Calculate percentage safely
            float percentage = result.getTotalMarks() > 0 ?
                    (result.getObtainedMarks() / result.getTotalMarks()) * 100 : 0;

            String subject = "Quiz Results: " + currentQuiz.getSubject();
            String body = String.format(Locale.getDefault(),
                    "Hello!\n\nYour Results:\n" +
                            "Score: %.1f/%.1f\n" +
                            "Percentage: %.1f%%\n\n" +
                            "Date: %s\n\n" +
                            "Thank you for participating!",
                    result.getObtainedMarks(),
                    result.getTotalMarks(),
                    percentage,
                    new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(new Date()));

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + email));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);

            // Verify email client availability
            PackageManager pm = getPackageManager();
            if (emailIntent.resolveActivity(pm) != null) {
                startActivity(emailIntent);
                return true;
            } else {
                showEmailAlternativeDialog(body);
                return false;
            }
        } catch (Exception e) {
            Log.e("EmailError", "Failed to send email: " + e.getMessage());
            return false;
        }
    }

    private void showEmailAlternativeDialog(String emailContent) {
        new AlertDialog.Builder(this)
                .setTitle("Email Not Sent")
                .setMessage("Couldn't launch email client.\n\nYour results:\n" + emailContent)
                .setPositiveButton("Copy", (dialog, which) -> {
                    copyToClipboard(emailContent);
                    Toast.makeText(this, "Results copied", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("OK", null)
                .show();
    }  private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Quiz Results", text);
        clipboard.setPrimaryClip(clip);
    }
}