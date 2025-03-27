package com.example.abcd.ExamQuizes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.abcd.QuizActivity;
import com.example.abcd.databinding.ActivityExamQuizesMainBinding;
import com.example.abcd.utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class examQuizesMainActivity extends AppCompatActivity
        implements QuizAdapter.QuizInteractionListener {

    private ActivityExamQuizesMainBinding binding;
    private QuizAdapter quizAdapter;
    private final List<Quiz> quizList = new ArrayList<>();
    private static final String TAG = "QuizMainActivity";
    private String userRole = "";
    private CardView resultsCardView;
    private RecyclerView resultsRecyclerView;
    private Button sendNotificationButton, closeButton;
    private ResultsAdapter resultsAdapter;
    private List<QuizResult> currentResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExamQuizesMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        setupFirebaseListener();

        // Initialize results UI components
        resultsCardView = binding.resultsCardView;
        resultsRecyclerView = binding.resultsRecyclerView;
        sendNotificationButton = binding.sendNotificationButton;
        closeButton = binding.closeButton;
        resultsAdapter = new ResultsAdapter(currentResults);
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resultsRecyclerView.setAdapter(resultsAdapter);

        // Set up button listeners
        sendNotificationButton.setOnClickListener(v -> {
            Toast.makeText(this, "Send notification clicked", Toast.LENGTH_SHORT).show();
            // Add notification logic here if specified
        });
        closeButton.setOnClickListener(v -> {
            resultsCardView.setVisibility(View.GONE);
            binding.fabCreateQuiz.setVisibility(View.VISIBLE);
        });

        // Hide the FAB initially until the role is verified
        binding.fabCreateQuiz.setVisibility(View.GONE);

        // Fetch user email from session and retrieve the user role
        SessionManager sessionManager = new SessionManager(this);
        String userEmail = sessionManager.getEmail();

        if (userEmail != null) {
            fetchUserRole(userEmail);
        } else {
            Toast.makeText(this, "User email not found in session", Toast.LENGTH_SHORT).show();
        }

        setupFab();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        quizAdapter = new QuizAdapter(quizList, this);
        binding.quizzesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.quizzesRecyclerView.setAdapter(quizAdapter);
    }

    private void setupFirebaseListener() {
        FirebaseDatabase.getInstance().getReference("quizzes")
                .addValueEventListener(new ValueEventListener() {
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
                        Toast.makeText(examQuizesMainActivity.this,
                                "Database error: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
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
                .orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String role = userSnapshot.child("userRole").getValue(String.class);
                            if (role != null) {
                                userRole = role.toLowerCase();
                                Log.d(TAG, "User role: " + userRole);
                                if ("teacher".equals(userRole)) {
                                    binding.fabCreateQuiz.setVisibility(View.VISIBLE);
                                } else {
                                    binding.fabCreateQuiz.setVisibility(View.GONE);
                                }
                                break;
                            }
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
        long currentTime = System.currentTimeMillis();
        if ("student".equals(userRole)) {
            if (isQuizActive(quiz)) {
                Intent intent = new Intent(this, QuizActivity.class);
                intent.putExtra("SELECTED_QUIZ", quiz);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Quiz has expired", Toast.LENGTH_SHORT).show();
            }
        } else if ("teacher".equals(userRole) || "admin".equals(userRole)) {
            showQuizResults(quiz);
        }
    }

    @Override
    public void onQuizLongClicked(Quiz quiz) {
        if (("teacher".equals(userRole) || "admin".equals(userRole)) && !isQuizActive(quiz)) {
            new AlertDialog.Builder(this)
                    .setTitle("Remove Quiz")
                    .setMessage("Are you sure you want to remove this expired quiz?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseDatabase.getInstance().getReference("quizzes")
                                .child(quiz.getQuizId())
                                .removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(this, "Quiz removed", Toast.LENGTH_SHORT).show();
                                        removeQuizFromList(quiz.getQuizId());
                                    } else {
                                        Toast.makeText(this, "Removal failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    private void showQuizResults(Quiz quiz) {
        DatabaseReference resultsRef = FirebaseDatabase.getInstance().getReference("quiz_results");
        resultsRef.orderByChild("subject").equalTo(quiz.getSubject())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentResults.clear();
                        for (DataSnapshot resultSnapshot : snapshot.getChildren()) {
                            QuizResult result = resultSnapshot.getValue(QuizResult.class);
                            if (result != null) {
                                currentResults.add(result);
                            }
                        }
                        if (currentResults.isEmpty()) {
                            binding.resultsTitle.setText("No results available for " + quiz.getSubject());
                        } else {
                            binding.resultsTitle.setText("Results for " + quiz.getSubject());
                        }
                        resultsAdapter.notifyDataSetChanged();
                        resultsCardView.setVisibility(View.VISIBLE);
                        binding.fabCreateQuiz.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(examQuizesMainActivity.this,
                                "Failed to load results: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
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
        return currentTime >= quiz.getStartingTime() &&
                currentTime <= quiz.getEndingTime();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}