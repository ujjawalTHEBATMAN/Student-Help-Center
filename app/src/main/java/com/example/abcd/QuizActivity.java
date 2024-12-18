package com.example.abcd;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;

public class QuizActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Initialize category cards
        CardView sem1Card = findViewById(R.id.sem1Card);
        CardView sem2Card = findViewById(R.id.sem2Card);
        CardView sem3Card = findViewById(R.id.sem3Card);
        CardView sem4Card = findViewById(R.id.sem4Card);
        CardView sem5Card = findViewById(R.id.sem5Card);
        CardView sem6Card = findViewById(R.id.sem6Card);

        // Set click listeners for each card
        sem1Card.setOnClickListener(v -> startQuiz("sem1"));
        sem2Card.setOnClickListener(v -> startQuiz("sem2"));
        sem3Card.setOnClickListener(v -> startQuiz("sem3"));
        sem4Card.setOnClickListener(v -> startQuiz("sem4"));
        sem5Card.setOnClickListener(v -> startQuiz("sem5"));
        sem6Card.setOnClickListener(v -> startQuiz("sem6"));
    }

    private void startQuiz(String semester) {
        Intent intent = new Intent(this, SubjectSelectionActivity.class);
        intent.putExtra("SEMESTER", semester);
        startActivity(intent);
    }
}
