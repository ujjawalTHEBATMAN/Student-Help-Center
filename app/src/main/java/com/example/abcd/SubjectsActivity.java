package com.example.abcd;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class SubjectsActivity extends AppCompatActivity {

    private RecyclerView subjectsRecyclerView;
    private SubjectAdapter adapter;
    private List<String> subjects;
    private String selectedSemester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjects);

        selectedSemester = getIntent().getStringExtra("SEMESTER");
        if (selectedSemester == null) {
            finish();
            return;
        }

        // Initialize views
        subjectsRecyclerView = findViewById(R.id.subjectsRecyclerView);
        subjectsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Initialize subjects list and adapter
        subjects = new ArrayList<>();
        adapter = new SubjectAdapter(subjects);
        subjectsRecyclerView.setAdapter(adapter);

        // Set title
        TextView titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText(selectedSemester + " Subjects");

        // Load subjects from Firebase
        loadSubjects();
    }

    private void loadSubjects() {
        DatabaseReference subjectsRef = FirebaseDatabase.getInstance()
                .getReference("subjects")
                .child(selectedSemester.toLowerCase().replace(" ", "_"));

        subjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjects.clear();
                for (DataSnapshot subjectSnapshot : snapshot.getChildren()) {
                    String subject = subjectSnapshot.getValue(String.class);
                    if (subject != null) {
                        subjects.add(subject);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {
        private final List<String> subjects;

        public SubjectAdapter(List<String> subjects) {
            this.subjects = subjects;
        }

        @NonNull
        @Override
        public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_subject, parent, false);
            return new SubjectViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
            String subject = subjects.get(position);
            holder.subjectName.setText(subject);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(SubjectsActivity.this, PDFViewerActivity.class);
                intent.putExtra("SEMESTER", selectedSemester);
                intent.putExtra("SUBJECT", subject);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return subjects.size();
        }

        private  class SubjectViewHolder extends RecyclerView.ViewHolder {
            TextView subjectName;

            public SubjectViewHolder(@NonNull View itemView) {
                super(itemView);
                subjectName = itemView.findViewById(R.id.subjectName);
            }
        }
    }
}
