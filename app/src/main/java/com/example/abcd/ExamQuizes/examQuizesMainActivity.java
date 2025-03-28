package com.example.abcd.ExamQuizes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.abcd.QuizActivity;
import com.example.abcd.R;
import com.example.abcd.databinding.ActivityExamQuizesMainBinding;
import com.example.abcd.utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class examQuizesMainActivity extends AppCompatActivity implements QuizAdapter.QuizInteractionListener {
    private ActivityExamQuizesMainBinding binding;
    private QuizAdapter quizAdapter;
    private final List<Quiz> quizList = new ArrayList<>();
    private static final String TAG = "QuizMainActivity";
    private String userRole = "";
    private String userEmail = "";
    private CardView resultsCardView;
    private RecyclerView resultsRecyclerView;
    private Button sendNotificationButton, closeButton;
    private ImageButton topCloseButton;
    private ResultsAdapter resultsAdapter;
    private List<QuizResult> currentResults = new ArrayList<>();
    private List<QuizAttempt> currentAttempts = new ArrayList<>();
    private ConstraintLayout mainLayout;
    private DatabaseReference quizzesRef;
    private DatabaseReference resultsRef;
    private DatabaseReference analysisRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExamQuizesMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase references
        quizzesRef = FirebaseDatabase.getInstance().getReference("quizzes");
        resultsRef = FirebaseDatabase.getInstance().getReference("quiz_results");
        analysisRef = FirebaseDatabase.getInstance().getReference("quizzesResultAnalysesData");

        // Initialize UI components
        mainLayout = findViewById(R.id.mainLayout);
        resultsCardView = binding.resultsCardView;
        resultsRecyclerView = binding.resultsRecyclerView;
        sendNotificationButton = binding.sendNotificationButton;
        closeButton = binding.closeButton;
        topCloseButton = findViewById(R.id.topCloseButton);

        // Validate UI components
        if (mainLayout == null || resultsCardView == null || resultsRecyclerView == null ||
                sendNotificationButton == null || closeButton == null || topCloseButton == null) {
            Toast.makeText(this, "Error initializing UI components", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupToolbar();
        setupRecyclerView();
        setupResultsView();
        setupFirebaseListener();
        setupButtonListeners();
        setupTouchListener();

        // Hide the FAB initially until the role is verified
        binding.fabCreateQuiz.setVisibility(View.GONE);

        // Fetch user email and role
        SessionManager sessionManager = new SessionManager(this);
        userEmail = sessionManager.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "User email not found in session", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        fetchUserRole(userEmail);
        setupFab();
    }

    private void setupToolbar() {
        if (binding.toolbar != null) {
            setSupportActionBar(binding.toolbar);
            binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        } else {
            Log.e(TAG, "Toolbar is null");
        }
    }

    private void setupRecyclerView() {
        if (binding.quizzesRecyclerView != null) {
            quizAdapter = new QuizAdapter(quizList, this);
            binding.quizzesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            binding.quizzesRecyclerView.setAdapter(quizAdapter);
        } else {
            Log.e(TAG, "Quizzes RecyclerView is null");
        }
    }

    private void setupResultsView() {
        resultsAdapter = new ResultsAdapter(currentResults, currentAttempts, userRole);
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resultsRecyclerView.setAdapter(resultsAdapter);
    }

    private void setupButtonListeners() {
        sendNotificationButton.setOnClickListener(v -> {
            if ("teacher".equals(userRole) || "admin".equals(userRole)) {
                Toast.makeText(this, "Send notification clicked", Toast.LENGTH_SHORT).show();
                // Add notification logic here if needed
            } else {
                Toast.makeText(this, "Only teachers/admins can send notifications", Toast.LENGTH_SHORT).show();
            }
        });

        closeButton.setOnClickListener(v -> {
            resultsCardView.setVisibility(View.GONE);
            binding.fabCreateQuiz.setVisibility("teacher".equals(userRole) ? View.VISIBLE : View.GONE);
        });

        topCloseButton.setOnClickListener(v -> {
            resultsCardView.setVisibility(View.GONE);
            binding.fabCreateQuiz.setVisibility("teacher".equals(userRole) ? View.VISIBLE : View.GONE);
        });
    }

    private void setupTouchListener() {
        mainLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && resultsCardView.getVisibility() == View.VISIBLE) {
                int[] location = new int[2];
                resultsCardView.getLocationOnScreen(location);
                int left = location[0];
                int top = location[1];
                int right = left + resultsCardView.getWidth();
                int bottom = top + resultsCardView.getHeight();

                if (!(event.getRawX() >= left && event.getRawX() <= right &&
                        event.getRawY() >= top && event.getRawY() <= bottom)) {
                    resultsCardView.setVisibility(View.GONE);
                    binding.fabCreateQuiz.setVisibility("teacher".equals(userRole) ? View.VISIBLE : View.GONE);
                    return true;
                }
            }
            return false;
        });
    }

    private void setupFirebaseListener() {
        quizzesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                quizList.clear();
                for (DataSnapshot quizSnapshot : snapshot.getChildren()) {
                    try {
                        Quiz quiz = quizSnapshot.getValue(Quiz.class);
                        if (quiz != null) {
                            quiz.setQuizId(quizSnapshot.getKey());
                            quizList.add(quiz);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing quiz", e);
                    }
                }

                quizAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(examQuizesMainActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupFab() {
        binding.fabCreateQuiz.setOnClickListener(v -> {
            if ("teacher".equals(userRole)) {
                startActivity(new Intent(this, CreateNewQuizes.class));
            } else {
                Toast.makeText(this, "Only teachers can create quizzes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserRole(String email) {
        FirebaseDatabase.getInstance().getReference("users")
                .child(email.replace(".", ","))
                .child("userRole")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String role = snapshot.getValue(String.class);
                        if (role != null) {
                            userRole = role.toLowerCase();
                            Log.d(TAG, "User role: " + userRole);
                            binding.fabCreateQuiz.setVisibility("teacher".equals(userRole) ? View.VISIBLE : View.GONE);
                        } else {
                            Log.w(TAG, "User role not found for email: " + email);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error fetching user role: " + error.getMessage());
                    }
                });
    }

    @Override
    public void onQuizClicked(Quiz quiz) {
        if (quiz == null) {
            Toast.makeText(this, "Invalid quiz data", Toast.LENGTH_SHORT).show();
            return;
        }

        long currentTime = System.currentTimeMillis();
        if ("student".equals(userRole)) {
            if (currentTime < quiz.getStartingTime()) {
                Toast.makeText(this, "Quiz has not started yet", Toast.LENGTH_SHORT).show();
            } else if (isQuizActive(quiz)) {
                checkStudentAttempt(quiz);
            } else {
                showStudentPreviousAttempt(quiz);
            }
        } else if ("teacher".equals(userRole) || "admin".equals(userRole)) {
            if (currentTime < quiz.getStartingTime()) {
                binding.resultsTitle.setText("Quiz not started: " + quiz.getSubject());
                currentResults.clear();
                currentAttempts.clear();
                resultsAdapter.notifyDataSetChanged();
                resultsCardView.setVisibility(View.VISIBLE);
                binding.fabCreateQuiz.setVisibility(View.GONE);
            } else {
                showQuizResults(quiz);
            }
        } else {
            Toast.makeText(this, "Unauthorized access", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onQuizLongClicked(Quiz quiz) {
        if (quiz == null) {
            Toast.makeText(this, "Invalid quiz data", Toast.LENGTH_SHORT).show();
            return;
        }

        if (("teacher".equals(userRole) || "admin".equals(userRole)) &&
                !isQuizActive(quiz) &&
                System.currentTimeMillis() >= quiz.getStartingTime()) {
            new AlertDialog.Builder(this)
                    .setTitle("Remove Quiz")
                    .setMessage("Are you sure you want to remove this expired quiz? This will also delete all related results and analysis.")
                    .setPositiveButton("Yes", (dialog, which) -> removeQuizAndRelatedData(quiz))
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    private void removeQuizAndRelatedData(Quiz quiz) {
        String subject = quiz.getSubject();
        String quizId = quiz.getQuizId();

        // Remove quiz
        quizzesRef.child(quizId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        removeQuizFromList(quizId);
                        deleteQuizResults(subject);
                        deleteQuizAnalysis(subject);
                        Toast.makeText(this, "Quiz and related data removed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to remove quiz", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteQuizResults(String subject) {
        resultsRef.orderByChild("subject").equalTo(subject)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot resultSnapshot : snapshot.getChildren()) {
                            resultSnapshot.getRef().removeValue()
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to delete result: " + e.getMessage()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error deleting quiz results: " + error.getMessage());
                    }
                });
    }

    private void deleteQuizAnalysis(String subject) {
        analysisRef.orderByChild("subject").equalTo(subject)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot analysisSnapshot : snapshot.getChildren()) {
                            analysisSnapshot.getRef().removeValue()
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to delete analysis: " + e.getMessage()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error deleting quiz analysis: " + error.getMessage());
                    }
                });
    }

    private void checkStudentAttempt(Quiz quiz) {
        if (userEmail.isEmpty()) {
            Toast.makeText(this, "User email not available", Toast.LENGTH_SHORT).show();
            return;
        }

        analysisRef.orderByChild("email").equalTo(userEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean hasAttempted = false;
                        for (DataSnapshot attemptSnapshot : snapshot.getChildren()) {
                            String subject = attemptSnapshot.child("subject").getValue(String.class);
                            if (subject != null && subject.equals(quiz.getSubject())) {
                                hasAttempted = true;
                                showStudentPreviousAttempt(quiz);
                                break;
                            }
                        }
                        if (!hasAttempted) {
                            Intent intent = new Intent(examQuizesMainActivity.this, QuizActivity.class);
                            intent.putExtra("SELECTED_QUIZ", quiz);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(examQuizesMainActivity.this, "Error checking attempt: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showStudentPreviousAttempt(Quiz quiz) {
        analysisRef.orderByChild("email").equalTo(userEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentAttempts.clear();
                        currentResults.clear();
                        for (DataSnapshot attemptSnapshot : snapshot.getChildren()) {
                            String subject = attemptSnapshot.child("subject").getValue(String.class);
                            if (subject != null && subject.equals(quiz.getSubject())) {
                                QuizAttempt attempt = attemptSnapshot.getValue(QuizAttempt.class);
                                if (attempt != null) {
                                    attempt.setAttemptId(attemptSnapshot.getKey());
                                    currentAttempts.add(attempt);
                                }
                            }
                        }
                        resultsRef.orderByChild("userEmail").equalTo(userEmail)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot resultSnapshot : snapshot.getChildren()) {
                                            QuizResult result = resultSnapshot.getValue(QuizResult.class);
                                            if (result != null && result.getSubject().equals(quiz.getSubject())) {
                                                currentResults.add(result);
                                            }
                                        }
                                        binding.resultsTitle.setText("Your Previous Attempt: " + quiz.getSubject());
                                        resultsAdapter.notifyDataSetChanged();
                                        resultsCardView.setVisibility(View.VISIBLE);
                                        binding.fabCreateQuiz.setVisibility(View.GONE);
                                        sendNotificationButton.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(examQuizesMainActivity.this, "Error loading results: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(examQuizesMainActivity.this, "Error loading attempt: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showQuizResults(Quiz quiz) {
        resultsRef.orderByChild("subject").equalTo(quiz.getSubject())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentResults.clear();
                        currentAttempts.clear();
                        for (DataSnapshot resultSnapshot : snapshot.getChildren()) {
                            QuizResult result = resultSnapshot.getValue(QuizResult.class);
                            if (result != null) {
                                currentResults.add(result);
                            }
                        }
                        binding.resultsTitle.setText(currentResults.isEmpty() ?
                                "No results available for " + quiz.getSubject() :
                                "Results for " + quiz.getSubject());
                        resultsAdapter.notifyDataSetChanged();
                        resultsCardView.setVisibility(View.VISIBLE);
                        binding.fabCreateQuiz.setVisibility(View.GONE);
                        sendNotificationButton.setVisibility("teacher".equals(userRole) || "admin".equals(userRole) ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(examQuizesMainActivity.this, "Failed to load results: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeQuizFromList(String quizId) {
        for (int i = 0; i < quizList.size(); i++) {
            if (quizList.get(i).getQuizId().equals(quizId)) {
                quizList.remove(i);
                quizAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    private boolean isQuizActive(Quiz quiz) {
        long currentTime = System.currentTimeMillis();
        return currentTime >= quiz.getStartingTime() && currentTime <= quiz.getEndingTime();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}