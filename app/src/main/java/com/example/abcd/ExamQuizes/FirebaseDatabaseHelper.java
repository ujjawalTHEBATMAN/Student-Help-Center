package com.example.abcd.ExamQuizes;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class FirebaseDatabaseHelper {
    private static final String QUIZZES_NODE = "quizzes";
    private final DatabaseReference databaseReference;

    public FirebaseDatabaseHelper() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void saveQuiz(Quiz quiz, DatabaseReference.CompletionListener listener) {
        DatabaseReference quizRef = databaseReference.child(QUIZZES_NODE).push();
        quiz.setQuizId(quizRef.getKey());
        quizRef.setValue(quiz.toMap(), listener);

        // Add server timestamp for audit
        quizRef.child("timestamp").setValue(ServerValue.TIMESTAMP);
    }
}