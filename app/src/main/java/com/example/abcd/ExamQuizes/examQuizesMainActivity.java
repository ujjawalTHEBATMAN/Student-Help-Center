package com.example.abcd.ExamQuizes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.abcd.QuizActivity;
import com.example.abcd.databinding.ActivityExamQuizesMainBinding;
import com.example.abcd.utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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

    // Hold the user's role fetched from Firebase
    private String userRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExamQuizesMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        setupFirebaseListener();
        setupFab();

        // Get user email from the session and then fetch the user role from Firebase.
        SessionManager sessionManager = new SessionManager(this);
        String userEmail = sessionManager.getEmail();
        if (userEmail != null) {
            fetchUserRole(userEmail);
        } else {
            Toast.makeText(this, "User email not found in session", Toast.LENGTH_SHORT).show();
        }
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
        binding.fabCreateQuiz.setOnClickListener(v ->
                startActivity(new Intent(this, CreateNewQuizes.class)));
    }

    /**
     * Fetches the user role from the Firebase realtime database based on the user's email.
     */
    private void fetchUserRole(String email) {
        FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Assuming email is unique, fetch the userRole from the first matching record.
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String role = userSnapshot.child("userRole").getValue(String.class);
                            if (role != null) {
                                userRole = role;
                                Log.d(TAG, "User role: " + userRole);
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
    public void onActiveQuizClicked(Quiz quiz) {
        if (isQuizActive(quiz)) {
            Intent intent = new Intent(this, QuizActivity.class);
            intent.putExtra("SELECTED_QUIZ", quiz);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Quiz is no longer available", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when an expired quiz card is clicked.
     * Only admin or teacher can delete the quiz from Firebase.
     */
    @Override
    public void onExpiredQuizClicked(String quizId) {
        // Check if the current user is allowed to delete expired quizzes.
        if ("admin".equalsIgnoreCase(userRole) || "teacher".equalsIgnoreCase(userRole)) {
            FirebaseDatabase.getInstance().getReference("quizzes")
                    .child(quizId)
                    .removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Quiz removed", Toast.LENGTH_SHORT).show();
                            removeQuizFromList(quizId);
                        } else {
                            Toast.makeText(this, "Removal failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Only admin or teacher can remove expired quizzes", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper method to remove a quiz from the local list and notify the adapter.
     */
    private void removeQuizFromList(String quizId) {
        for (int i = 0; i < quizList.size(); i++) {
            if (quizList.get(i).getQuizId().equals(quizId)) {
                quizList.remove(i);
                quizAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    /**
     * Checks if the quiz is active based on the current system time.
     */
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
