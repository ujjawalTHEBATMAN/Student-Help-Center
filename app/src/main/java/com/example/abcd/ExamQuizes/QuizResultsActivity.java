package com.example.abcd.ExamQuizes;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.abcd.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class QuizResultsActivity extends AppCompatActivity {

    private QuizResult currentResult;
    private DatabaseReference resultsRef;
    private LeaderboardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_results);

        currentResult = getIntent().getParcelableExtra("quiz_result");
        resultsRef = FirebaseDatabase.getInstance().getReference("quiz_results");

        setupViews();
        loadLeaderboard();
    }

    private void setupViews() {
        TextView tvScore = findViewById(R.id.tv_score);
        TextView tvRank = findViewById(R.id.tv_rank);
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        RecyclerView leaderboardView = findViewById(R.id.rv_leaderboard);

        // Handle division by zero case
        float percentage = 0;
        if (currentResult.getTotalMarks() > 0) {
            percentage = (currentResult.getObtainedMarks() / currentResult.getTotalMarks()) * 100;
        }

        tvScore.setText(String.format(Locale.getDefault(),
                "Score: %.1f/%.1f",
                currentResult.getObtainedMarks(),
                currentResult.getTotalMarks()));

        progressBar.setProgress((int) percentage);

        leaderboardView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(new ArrayList<>());
        leaderboardView.setAdapter(adapter);
    }

    private void loadLeaderboard() {
        resultsRef.orderByChild("obtainedMarks").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<QuizResult> allResults = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    QuizResult result = data.getValue(QuizResult.class);
                    if (result != null) allResults.add(result);
                }

                // Correct sorting: Descending order (highest first)
                Collections.sort(allResults, (r1, r2) ->
                        Float.compare(r2.getObtainedMarks(), r1.getObtainedMarks()));

                updateRank(allResults);
                updateLeaderboard(allResults);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                TextView tvRank = findViewById(R.id.tv_rank);
                tvRank.setText("Rank: N/A");
            }
        });
    }

    private void updateRank(List<QuizResult> allResults) {
        TextView tvRank = findViewById(R.id.tv_rank);
        int rank = 1;
        for (int i = 0; i < allResults.size(); i++) {
            if (allResults.get(i).getUserEmail().equals(currentResult.getUserEmail())) {
                rank = i + 1;
                break;
            }
        }
        tvRank.setText(String.format(Locale.getDefault(), "Rank: %d", rank));
    }

    private void updateLeaderboard(List<QuizResult> allResults) {
        // Get top 10 without reversing
        List<QuizResult> top10 = allResults.size() > 10
                ? allResults.subList(0, 10)
                : allResults;

        adapter.updateData(top10);
    }
}