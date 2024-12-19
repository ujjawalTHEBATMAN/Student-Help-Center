package com.example.abcd;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class SemestersActivity extends AppCompatActivity {

    private RecyclerView semestersRecyclerView;
    private final List<String> semesters = Arrays.asList(
            "Semester 1", "Semester 2", "Semester 3",
            "Semester 4", "Semester 5", "Semester 6"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_semesters);

        semestersRecyclerView = findViewById(R.id.semestersRecyclerView);
        semestersRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        semestersRecyclerView.setAdapter(new SemesterAdapter(semesters));
    }

    private class SemesterAdapter extends RecyclerView.Adapter<SemesterAdapter.SemesterViewHolder> {
        private final List<String> semesters;

        public SemesterAdapter(List<String> semesters) {
            this.semesters = semesters;
        }

        @NonNull
        @Override
        public SemesterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_semester, parent, false);
            return new SemesterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SemesterViewHolder holder, int position) {
            String semester = semesters.get(position);
            holder.semesterTextView.setText(semester);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(SemestersActivity.this, SubjectsActivity.class);
                intent.putExtra("SEMESTER", semester);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return semesters.size();
        }

        private class SemesterViewHolder extends RecyclerView.ViewHolder {
            TextView semesterTextView;

            public SemesterViewHolder(@NonNull View itemView) {
                super(itemView);
                semesterTextView = itemView.findViewById(R.id.semesterTextView);
            }
        }
    }
}
