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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExamQuizesMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        setupFirebaseListener();
        setupFab();
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

    @Override
    public void onExpiredQuizClicked(String quizId) {
        FirebaseDatabase.getInstance().getReference("quizzes")
                .child(quizId)
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Quiz removed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Removal failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isQuizActive(Quiz quiz) {
        long currentTime = System.currentTimeMillis();
        return currentTime >= quiz.getStartingTime() &&
                currentTime <= quiz.getEndingTime(); // Use getEndingTime()
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}