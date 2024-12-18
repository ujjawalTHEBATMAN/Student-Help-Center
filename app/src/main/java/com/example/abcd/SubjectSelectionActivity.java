package com.example.abcd;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.example.abcd.models.QuizManager;
import java.util.List;

public class SubjectSelectionActivity extends AppCompatActivity {
    private String selectedSemester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_selection);

        selectedSemester = getIntent().getStringExtra("SEMESTER");
        RecyclerView subjectsRecyclerView = findViewById(R.id.subjectsRecyclerView);
        
        List<String> subjects = QuizManager.getSubjectsForSemester(selectedSemester);
        
        SubjectAdapter adapter = new SubjectAdapter(subjects, subject -> {
            Intent intent = new Intent(this, QuizQuestionsActivity.class);
            intent.putExtra("SEMESTER", selectedSemester);
            intent.putExtra("SUBJECT", subject);
            startActivity(intent);
        });

        subjectsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        subjectsRecyclerView.setAdapter(adapter);
    }

    private static class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {
        private final List<String> subjects;
        private final OnSubjectClickListener listener;

        public SubjectAdapter(List<String> subjects, OnSubjectClickListener listener) {
            this.subjects = subjects;
            this.listener = listener;
        }

        @Override
        public SubjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_subject, parent, false);
            return new SubjectViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SubjectViewHolder holder, int position) {
            String subject = subjects.get(position);
            holder.subjectName.setText(subject);
            holder.cardView.setOnClickListener(v -> listener.onSubjectClick(subject));
        }

        @Override
        public int getItemCount() {
            return subjects.size();
        }

        static class SubjectViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView subjectName;

            SubjectViewHolder(View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.subjectCard);
                subjectName = itemView.findViewById(R.id.subjectName);
            }
        }
    }

    interface OnSubjectClickListener {
        void onSubjectClick(String subject);
    }
}
