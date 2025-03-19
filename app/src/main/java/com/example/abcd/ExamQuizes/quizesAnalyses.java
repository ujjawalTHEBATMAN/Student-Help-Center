package com.example.abcd.ExamQuizes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.abcd.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class quizesAnalyses extends AppCompatActivity {

    private RecyclerView rvQuizAnalysis;
    private TextView tvSubjectTitle, tvEmailInfo, tvResultSummary;
    private View progressBar;
    private String userEmail, subject;
    private List<QuizQuestion1> quizQuestions;
    private QuizAnalysisAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quizes_analyses);

        // Initialize views
        rvQuizAnalysis = findViewById(R.id.rvQuizAnalysis);
        tvSubjectTitle = findViewById(R.id.tvSubjectTitle);
        tvEmailInfo = findViewById(R.id.tvEmailInfo);
        tvResultSummary = findViewById(R.id.tvResultSummary);
        progressBar = findViewById(R.id.progressBar);
        Button btnNext = findViewById(R.id.btnNext);

        // Get data from intent with null checks
        userEmail = getIntent().getStringExtra("email");
        subject = getIntent().getStringExtra("subject");

        // Validate required data
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (subject == null || subject.isEmpty()) {
            Toast.makeText(this, "Subject not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup RecyclerView and adapter
        quizQuestions = new ArrayList<>();
        adapter = new QuizAnalysisAdapter(quizQuestions);
        rvQuizAnalysis.setLayoutManager(new LinearLayoutManager(this));
        rvQuizAnalysis.setAdapter(adapter);

        tvSubjectTitle.setText("Subject: " + subject);
        tvEmailInfo.setText("Email: " + userEmail);

        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(quizesAnalyses.this, QuizResultsActivity.class);
            intent.putExtra("email", userEmail);
            intent.putExtra("subject", subject);
            startActivity(intent);
        });

        fetchQuizData();
    }

    private void fetchQuizData() {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("quizzesResultAnalysesData");

        // Additional null check for userEmail
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "User email not available", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        Query query = databaseRef.orderByChild("email").equalTo(userEmail);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<QuizQuestion1> tempList = new ArrayList<>();
                int totalQuestions = 0;
                int correctAnswers = 0;

                // Process each quiz snapshot
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String dataSubject = snapshot.child("subject").getValue(String.class);
                    if (subject.equals(dataSubject)) {
                        DataSnapshot questionsSnapshot = snapshot.child("questions");
                        if (questionsSnapshot.exists()) {
                            for (DataSnapshot questionData : questionsSnapshot.getChildren()) {
                                String question = questionData.child("question").getValue(String.class);
                                String userAnswer = questionData.child("userAnswer").getValue(String.class);
                                String correctAnswer = questionData.child("correctAnswer").getValue(String.class);

                                QuizQuestion1 quizQuestion = new QuizQuestion1(question, userAnswer, correctAnswer);
                                tempList.add(quizQuestion);
                                totalQuestions++;
                                if (quizQuestion.isCorrect()) {
                                    correctAnswers++;
                                }
                            }
                        }
                    }
                }

                // Use advanced filtering from last to first so that the last similar question survives.
                quizQuestions.clear();
                quizQuestions.addAll(removeSimilarQuestions(tempList));

                // Update result summary
                if (totalQuestions > 0) {
                    double percentage = (double) correctAnswers / totalQuestions * 100;
                    tvResultSummary.setText(String.format("Score: %d/%d (%.1f%%)", correctAnswers, totalQuestions, percentage));
                } else {
                    tvResultSummary.setText("No quiz data found");
                }

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(quizesAnalyses.this, "Error loading quiz data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    // Filtering method: Iterates from last to first so the last occurrence survives
    private List<QuizQuestion1> removeSimilarQuestions(List<QuizQuestion1> questions) {
        List<QuizQuestion1> filtered = new ArrayList<>();
        for (int i = questions.size() - 1; i >= 0; i--) {
            QuizQuestion1 current = questions.get(i);
            boolean isDuplicate = false;
            for (QuizQuestion1 existing : filtered) {
                if (isSimilar(current.getQuestion(), existing.getQuestion())) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                filtered.add(current);
            }
        }
        // Reverse the list to maintain the original order where the last similar question survives
        Collections.reverse(filtered);
        return filtered;
    }

    // Determines if two questions are similar based on an 80% similarity threshold
    private boolean isSimilar(String s1, String s2) {
        if (s1 == null || s2 == null) return false;
        int distance = computeLevenshteinDistance(s1, s2);
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return true;
        double similarity = (double) (maxLen - distance) / maxLen;
        return similarity >= 0.8; // threshold can be adjusted as needed
    }

    // Levenshtein distance algorithm to measure similarity between two strings
    private int computeLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1,
                                dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost);
            }
        }
        return dp[s1.length()][s2.length()];
    }
}
