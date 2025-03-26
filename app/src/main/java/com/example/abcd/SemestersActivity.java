package com.example.abcd;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SemestersActivity extends AppCompatActivity {
    private static final String TAG = "SemestersActivity";
    private RecyclerView semestersRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialToolbar topAppBar;
    private Logger errorLogger;

    private final List<String> semesters = Arrays.asList(
            "Semester 1", "Semester 2", "Semester 3",
            "Semester 4", "Semester 5", "Semester 6"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_semesters);

        initializeLogger();
        initializeViews();
        setupRecyclerView();
        setupAppBar();
        setupSwipeRefresh();
    }

    private void initializeLogger() {
        try {
            errorLogger = Logger.getLogger("ErrorLogger");
            FileHandler fileHandler = new FileHandler(
                    getApplicationContext().getFilesDir() + "/app_errors.log",
                    true
            );
            fileHandler.setFormatter(new SimpleFormatter());
            errorLogger.addHandler(fileHandler);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing logger", e);
        }
    }

    private void initializeViews() {
        semestersRecyclerView = findViewById(R.id.semestersRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        topAppBar = findViewById(R.id.topAppBar);
    }

    private void setupRecyclerView() {
        try {
            semestersRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            semestersRecyclerView.setAdapter(new SemesterAdapter(semesters));
        } catch (Exception e) {
            errorLogger.severe("RecyclerView setup error: " + e.getMessage());
            showErrorSnackbar("Unable to load semesters");
        }
    }

    private void setupAppBar() {
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Simulate refresh - in real app, you might reload data
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void showErrorSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content),
                message,
                Snackbar.LENGTH_SHORT
        ).show();
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
                try {
                    Intent intent = new Intent(SemestersActivity.this, SubjectsActivity.class);
                    intent.putExtra("SEMESTER", semester);
                    startActivity(intent);
                } catch (Exception e) {
                    errorLogger.severe("Navigation error: " + e.getMessage());
                    showErrorSnackbar("Unable to open semester");
                }
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

    @Override
    public void onBackPressed() {
        // Optional: Add transition animation or custom back press behavior
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}