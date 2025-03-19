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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class QuizResultsActivity extends AppCompatActivity {

    private DatabaseReference resultsRef;
    private LeaderboardAdapter adapter;
    private String userEmail, subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_results);

        userEmail = getIntent().getStringExtra("email");
        subject = getIntent().getStringExtra("subject");
        resultsRef = FirebaseDatabase.getInstance().getReference("quiz_results");

        setupViews();
        loadLeaderboard();
    }

    private void setupViews() {
        RecyclerView leaderboardView = findViewById(R.id.rv_leaderboard);
        leaderboardView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(new ArrayList<>());
        leaderboardView.setAdapter(adapter);
    }

    private void loadLeaderboard() {
        resultsRef.orderByChild("subject").equalTo(subject).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, QuizResult> uniqueResults = new HashMap<>();

                for (DataSnapshot data : snapshot.getChildren()) {
                    QuizResult result = data.getValue(QuizResult.class);
                    if (result != null) {
                        String email = result.getUserEmail();

                        // Only keep the highest score for each user
                        if (!uniqueResults.containsKey(email) ||
                                result.getObtainedMarks() > uniqueResults.get(email).getObtainedMarks()) {
                            uniqueResults.put(email, result);
                        }
                    }
                }

                List<QuizResult> uniqueList = new ArrayList<>(uniqueResults.values());

                // Sort by highest marks
                Collections.sort(uniqueList, (r1, r2) ->
                        Float.compare(r2.getObtainedMarks(), r1.getObtainedMarks()));

                updateUI(uniqueList);
                updateRank(uniqueList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                TextView tvRank = findViewById(R.id.tv_rank);
                tvRank.setText("Rank: N/A");
            }
        });
    }

    private void updateUI(List<QuizResult> results) {
        TextView tvScore = findViewById(R.id.tv_score);
        ProgressBar progressBar = findViewById(R.id.progress_bar);

        for (QuizResult result : results) {
            if (result.getUserEmail().equals(userEmail)) {
                float percentage = (result.getObtainedMarks() / result.getTotalMarks()) * 100;

                tvScore.setText(String.format(Locale.getDefault(),
                        "Score: %.1f/%.1f",
                        result.getObtainedMarks(),
                        result.getTotalMarks()));

                progressBar.setProgress((int) percentage);
                break;
            }
        }

        adapter.updateData(results);
    }

    private void updateRank(List<QuizResult> allResults) {
        TextView tvRank = findViewById(R.id.tv_rank);

        int rank = 1;
        for (QuizResult result : allResults) {
            if (result.getUserEmail().equals(userEmail)) {
                tvRank.setText(String.format(Locale.getDefault(), "Rank: %d", rank));
                return;
            }
            rank++;
        }

        tvRank.setText("Rank: N/A");
    }
}
