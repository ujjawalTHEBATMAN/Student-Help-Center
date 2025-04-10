package com.example.abcd;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.abcd.ExamQuizes.Question;
import com.example.abcd.ExamQuizes.Quiz;
import com.example.abcd.ExamQuizes.QuizResult;
import com.example.abcd.ExamQuizes.examQuizesMainActivity;
import com.example.abcd.ExamQuizes.quizesAnalyses;
import com.example.abcd.R;
import com.example.abcd.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class QuizActivity extends AppCompatActivity {

    private TextView tvTimer;
    private TextView tvQuestionProgress;
    private TextView tvQuestion;
    private ProgressBar progressBar;
    private TextView tvPointsInfo;
    private TextView tvOption1, tvOption2, tvOption3, tvOption4;
    private MaterialCardView optionCard1, optionCard2, optionCard3, optionCard4;
    private RadioButton rbOption1, rbOption2, rbOption3, rbOption4;
    private Button btnPrevious, btnNext, btnSubmit;
    private Quiz currentQuiz;
    private CountDownTimer quizTimer;
    private int currentQuestionIndex = 0;
    private List<Integer> selectedAnswers;
    private static final long TIMER_UPDATE_INTERVAL = 1000L;
    private static final String TAG = "QuizActivity";
    private SessionManager sessionManager;
    private DatabaseReference resultsRef;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        initializeViews();

        sessionManager = new SessionManager(this);
        resultsRef = FirebaseDatabase.getInstance().getReference("quiz_results");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        currentQuiz = getIntent().getParcelableExtra("SELECTED_QUIZ");
        if (currentQuiz == null) {
            Toast.makeText(this, "Error loading quiz data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupQuiz();
        setupNavigationButtons();
        setupTimer();
        setupOptionCards();
        loadCurrentQuestion();
    }

    private void initializeViews() {
        tvPointsInfo = findViewById(R.id.tvPointsInfo);
        tvTimer = findViewById(R.id.tvTimer);
        tvQuestionProgress = findViewById(R.id.tvQuestionProgress);
        tvQuestion = findViewById(R.id.tvQuestion);
        progressBar = findViewById(R.id.progressBar);
        optionCard1 = findViewById(R.id.optionCard1);
        optionCard2 = findViewById(R.id.optionCard2);
        optionCard3 = findViewById(R.id.optionCard3);
        optionCard4 = findViewById(R.id.optionCard4);
        rbOption1 = findViewById(R.id.rbOption1);
        rbOption2 = findViewById(R.id.rbOption2);
        rbOption3 = findViewById(R.id.rbOption3);
        rbOption4 = findViewById(R.id.rbOption4);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvOption1 = findViewById(R.id.tvOption1);
        tvOption2 = findViewById(R.id.tvOption2);
        tvOption3 = findViewById(R.id.tvOption3);
        tvOption4 = findViewById(R.id.tvOption4);
    }

    private void setupOptionCards() {
        View.OnClickListener cardClickListener = v -> {
            clearCardSelections();
            MaterialCardView card = (MaterialCardView) v;
            card.setCardBackgroundColor(getColor(R.color.selectedOptionBackground));
            card.setStrokeWidth(2);

            int selectedIndex = -1;
            if (v == optionCard1) {
                rbOption1.setChecked(true);
                selectedIndex = 0;
            } else if (v == optionCard2) {
                rbOption2.setChecked(true);
                selectedIndex = 1;
            } else if (v == optionCard3) {
                rbOption3.setChecked(true);
                selectedIndex = 2;
            } else if (v == optionCard4) {
                rbOption4.setChecked(true);
                selectedIndex = 3;
            }

            if (currentQuestionIndex >= 0 && currentQuestionIndex < selectedAnswers.size()) {
                selectedAnswers.set(currentQuestionIndex, selectedIndex);
            }
        };

        optionCard1.setOnClickListener(cardClickListener);
        optionCard2.setOnClickListener(cardClickListener);
        optionCard3.setOnClickListener(cardClickListener);
        optionCard4.setOnClickListener(cardClickListener);
    }

    private void clearCardSelections() {
        int defaultColor = getColor(R.color.optionCardBackground);
        optionCard1.setCardBackgroundColor(defaultColor);
        optionCard2.setCardBackgroundColor(defaultColor);
        optionCard3.setCardBackgroundColor(defaultColor);
        optionCard4.setCardBackgroundColor(defaultColor);

        optionCard1.setStrokeWidth(1);
        optionCard2.setStrokeWidth(1);
        optionCard3.setStrokeWidth(1);
        optionCard4.setStrokeWidth(1);

        rbOption1.setChecked(false);
        rbOption2.setChecked(false);
        rbOption3.setChecked(false);
        rbOption4.setChecked(false);
    }

    private void setupQuiz() {
        selectedAnswers = new ArrayList<>();
        for (int i = 0; i < currentQuiz.getQuestions().size(); i++) {
            selectedAnswers.add(-1);
        }
        progressBar.setMax(currentQuiz.getQuestions().size());

        float marksPerQuestion = currentQuiz.getMarksPerQuestion();
        String pointsText = String.format(Locale.getDefault(), "%.1f points per question", marksPerQuestion);
        tvPointsInfo.setText(pointsText);
    }

    private void setupTimer() {
        long quizDuration = currentQuiz.getEndingTime() - System.currentTimeMillis();
        if (quizDuration <= 0) {
            Toast.makeText(this, "Quiz time has expired!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        quizTimer = new CountDownTimer(quizDuration, TIMER_UPDATE_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;
                String timeRemaining = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                tvTimer.setText(timeRemaining);
            }

            @Override
            public void onFinish() {
                Toast.makeText(QuizActivity.this, "Time's up! Submitting quiz...", Toast.LENGTH_SHORT).show();
                submitQuiz();
            }
        }.start();
    }

    private void setupNavigationButtons() {
        btnPrevious.setOnClickListener(v -> {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                loadCurrentQuestion();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (selectedAnswers.get(currentQuestionIndex) == -1) {
                Toast.makeText(QuizActivity.this, "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentQuestionIndex < currentQuiz.getQuestions().size() - 1) {
                animateOptionsOut(() -> {
                    currentQuestionIndex++;
                    loadCurrentQuestion();
                    animateOptionsIn();
                });
            }
        });

        btnSubmit.setOnClickListener(v -> {
            boolean allAnswered = true;
            for (int answer : selectedAnswers) {
                if (answer == -1) {
                    allAnswered = false;
                    break;
                }
            }
            if (!allAnswered) {
                Toast.makeText(QuizActivity.this, "Please answer all questions before submitting", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(QuizActivity.this)
                    .setTitle("Submit Quiz")
                    .setMessage("Are you sure you want to submit this quiz?")
                    .setPositiveButton("Yes", (dialog, which) -> submitQuiz())
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void loadCurrentQuestion() {
        if (currentQuiz.getQuestions() == null || currentQuiz.getQuestions().isEmpty() || currentQuestionIndex >= currentQuiz.getQuestions().size()) {
            Toast.makeText(this, "Error loading question", Toast.LENGTH_SHORT).show();
            return;
        }

        Question question = currentQuiz.getQuestions().get(currentQuestionIndex);
        clearCardSelections();
        tvQuestion.setText(question.getText());

        Map<String, String> options = question.getOptions();
        if (options != null) {
            tvOption1.setText(options.get("option1"));
            tvOption2.setText(options.get("option2"));
            tvOption3.setText(options.get("option3"));
            tvOption4.setText(options.get("option4"));

            rbOption1.setText("");
            rbOption2.setText("");
            rbOption3.setText("");
            rbOption4.setText("");
        }
        int selectedOption = selectedAnswers.get(currentQuestionIndex);
        if (selectedOption != -1) {
            MaterialCardView selectedCard = null;
            switch (selectedOption) {
                case 0: selectedCard = optionCard1; rbOption1.setChecked(true); break;
                case 1: selectedCard = optionCard2; rbOption2.setChecked(true); break;
                case 2: selectedCard = optionCard3; rbOption3.setChecked(true); break;
                case 3: selectedCard = optionCard4; rbOption4.setChecked(true); break;
            }
            if (selectedCard != null) {
                selectedCard.setCardBackgroundColor(getColor(R.color.selectedOptionBackground));
                selectedCard.setStrokeWidth(2);
            }
        }
        updateNavigationControls();
    }

    private void updateNavigationControls() {
        String progressText = String.format(Locale.getDefault(), "Question %d/%d", currentQuestionIndex + 1, currentQuiz.getQuestions().size());
        tvQuestionProgress.setText(progressText);
        progressBar.setProgress(currentQuestionIndex + 1);
        btnPrevious.setEnabled(currentQuestionIndex > 0);
        if (currentQuestionIndex == currentQuiz.getQuestions().size() - 1) {
            btnNext.setVisibility(View.GONE);
            btnSubmit.setVisibility(View.VISIBLE);
        } else {
            btnNext.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.GONE);
        }
    }

    private void animateOptionsOut(Runnable onAnimationEnd) {
        int duration = 300;
        animateCard(optionCard1, 0f, duration);
        animateCard(optionCard2, 0f, duration);
        animateCard(optionCard3, 0f, duration);
        animateCard(optionCard4, 0f, duration);
        new Handler(Looper.getMainLooper()).postDelayed(onAnimationEnd, duration);
    }

    private void animateOptionsIn() {
        int duration = 300;
        animateCard(optionCard1, 1f, duration);
        animateCard(optionCard2, 1f, duration);
        animateCard(optionCard3, 1f, duration);
        animateCard(optionCard4, 1f, duration);
    }

    private void animateCard(MaterialCardView card, float alpha, int duration) {
        card.setAlpha(0f);
        card.setVisibility(View.VISIBLE);
        card.animate().alpha(alpha).setDuration(duration).start();
    }

    private void submitQuiz() {
        if (quizTimer != null) {
            quizTimer.cancel();
        }

        if (currentQuiz == null) {
            Toast.makeText(this, "Quiz data not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        QuizResult result = calculateResult();
        saveResultToFirebase(result);
        updateUserProfileStats(result);
        saveQuizAnalysisData();
    }

    private QuizResult calculateResult() {
        float obtainedMarks = 0;
        float marksPerQuestion = currentQuiz.getMarksPerQuestion();
        float negativeMarking = currentQuiz.getNegativeMarking();
        List<Question> questions = currentQuiz.getQuestions();

        if (questions == null) questions = new ArrayList<>();

        float totalPossibleMarks = questions.size() * marksPerQuestion;

        for (int i = 0; i < questions.size(); i++) {
            int selectedOption = selectedAnswers.get(i);
            int correctOption = questions.get(i).getCorrectAnswerIndex();
            if (selectedOption == correctOption) {
                obtainedMarks += marksPerQuestion;
            } else if (selectedOption != -1) {
                obtainedMarks -= negativeMarking;
            }
        }
        obtainedMarks = Math.max(0, obtainedMarks);

        String email = sessionManager != null ? sessionManager.getEmail() : "unknown@user.com";

        return new QuizResult(
                email,
                currentQuiz.getSubject(),
                obtainedMarks,
                totalPossibleMarks,
                System.currentTimeMillis()
        );
    }

    private void updateUserProfileStats(QuizResult result) {
        String email = sessionManager.getEmail();
        if (email == null) return;

        String emailKey = email.replace(".", ",");
        DatabaseReference userRef = usersRef.child(emailKey).child("stats");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get current values
                Long earnedPoints = dataSnapshot.child("earnedPoints").getValue(Long.class);
                Long totalPoints = dataSnapshot.child("totalPoints").getValue(Long.class);
                Long totalQuizzes = dataSnapshot.child("totalQuizzes").getValue(Long.class);

                // Set default values if null
                if (earnedPoints == null) earnedPoints = 0L;
                if (totalPoints == null) totalPoints = 0L;
                if (totalQuizzes == null) totalQuizzes = 0L;

                // Update values
                Map<String, Object> updates = new HashMap<>();
                updates.put("earnedPoints", earnedPoints + (long)result.getObtainedMarks());
                updates.put("totalPoints", totalPoints + (long)result.getTotalMarks());
                updates.put("totalQuizzes", totalQuizzes + 1);

                // Save to Firebase
                userRef.updateChildren(updates)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "User stats updated successfully"))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to update user stats: " + e.getMessage()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read user stats: " + databaseError.getMessage());
            }
        });
    }

    private void saveResultToFirebase(QuizResult result) {
        String resultId = resultsRef.push().getKey();
        if (resultId != null) {
            resultsRef.child(resultId).setValue(result)
                    .addOnSuccessListener(aVoid -> showResults(result))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save result: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        showResults(result);
                    });
        } else {
            Toast.makeText(this, "Failed to create result ID", Toast.LENGTH_SHORT).show();
            showResults(result);
        }
    }

    private void showResults(QuizResult result) {
        String email = sessionManager.getEmail();
        if (email == null || email.isEmpty()) {
            email = "unknown@user.com";
        }
        String subject = currentQuiz != null ? currentQuiz.getSubject() : "Unknown Subject";

        Intent analysisIntent = new Intent(this, quizesAnalyses.class);
        analysisIntent.putExtra("email", email);
        analysisIntent.putExtra("subject", subject);
        analysisIntent.putExtra("obtainedMarks", result.getObtainedMarks());
        analysisIntent.putExtra("totalMarks", result.getTotalMarks());
        startActivity(analysisIntent);
        finish();
    }

    private void saveQuizAnalysisData() {
        DatabaseReference analysisRef = FirebaseDatabase.getInstance().getReference("quizzesResultAnalysesData");
        String email = sessionManager.getEmail();
        String subject = currentQuiz.getSubject();
        List<Map<String, Object>> questionsAnalysis = new ArrayList<>();

        for (int i = 0; i < currentQuiz.getQuestions().size(); i++) {
            Question question = currentQuiz.getQuestions().get(i);
            Map<String, String> options = question.getOptions();
            int userSelectedIndex = selectedAnswers.get(i);
            String userAnswerText = "No Answer";
            if (userSelectedIndex >= 0) {
                userAnswerText = options.get("option" + (userSelectedIndex + 1));
            }
            int correctIndex = question.getCorrectAnswerIndex();
            String correctAnswerText = options.get("option" + (correctIndex + 1));

            Map<String, Object> questionData = new HashMap<>();
            questionData.put("question", question.getText());
            questionData.put("options", options);
            questionData.put("userAnswer", userAnswerText);
            questionData.put("correctAnswer", correctAnswerText);
            questionsAnalysis.add(questionData);
        }

        Map<String, Object> analysisData = new HashMap<>();
        analysisData.put("email", email);
        analysisData.put("subject", subject);
        analysisData.put("questions", questionsAnalysis);

        analysisRef.push().setValue(analysisData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Quiz analysis data saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save quiz analysis data", e));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (quizTimer != null) {
            quizTimer.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Quiz")
                .setMessage("Are you sure you want to exit? Your progress will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (quizTimer != null) {
                        quizTimer.cancel();
                    }
                    QuizActivity.super.onBackPressed();
                })
                .setNegativeButton("No", null)
                .show();
    }
}