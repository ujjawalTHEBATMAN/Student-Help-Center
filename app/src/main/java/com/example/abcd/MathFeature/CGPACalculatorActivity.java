package com.example.abcd.MathFeature;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CGPACalculatorActivity extends AppCompatActivity {

    private RecyclerView subjectsRecyclerView;
    private SubjectsAdapter subjectsAdapter;
    private LinearLayout subjectResults;
    private MaterialCardView resultsCard;
    private CircularProgressIndicator cgpaProgress;
    private TextView tvOverallCgpa;
    private MaterialButton btnCalculate;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cgpacalculator);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        initializeViews();
        setupRecyclerView();
        setupButtonClickListeners();
    }

    private void initializeViews() {
        subjectsRecyclerView = findViewById(R.id.subjects_recycler_view);
        subjectResults = findViewById(R.id.subject_results);
        resultsCard = findViewById(R.id.results_card);
        cgpaProgress = findViewById(R.id.cgpa_progress);
        tvOverallCgpa = findViewById(R.id.tv_overall_cgpa);
        btnCalculate = findViewById(R.id.btn_calculate);
        cgpaProgress.setMax(100);
        resultsCard.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        subjectsAdapter = new SubjectsAdapter();
        subjectsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        subjectsRecyclerView.setAdapter(subjectsAdapter);
        subjectsAdapter.addSubject();
    }

    private void setupButtonClickListeners() {
        findViewById(R.id.btn_add_subject).setOnClickListener(v -> subjectsAdapter.addSubject());
        btnCalculate.setOnClickListener(v -> calculateCGPA());
    }

    private void calculateCGPA() {
        btnCalculate.setEnabled(false);
        cgpaProgress.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            try {
                Deque<SubjectData> validSubjects = subjectsAdapter.getValidSubjects();
                if (validSubjects.isEmpty()) {
                    mainHandler.post(() -> showError("Please enter at least one valid subject"));
                    return;
                }
                double totalWeightedPoints = 0;
                int totalCredits = 0;
                subjectResults.removeAllViews();
                for (SubjectData subject : validSubjects) {
                    double percentage = (subject.obtainedMarks / subject.totalMarks) * 100;
                    double subjectCGPA = calculateSubjectCGPA(percentage);
                    totalWeightedPoints += subjectCGPA * subject.credits;
                    totalCredits += subject.credits;
                    mainHandler.post(() -> addSubjectResultView(subject.name, subject.credits, subjectCGPA));
                }
                double overallCGPA = totalWeightedPoints / totalCredits;
                mainHandler.post(() -> displayOverallCGPA(overallCGPA));
            } catch (Exception e) {
                mainHandler.post(() -> showError("Invalid input: " + e.getMessage()));
            } finally {
                mainHandler.post(() -> {
                    cgpaProgress.setVisibility(View.GONE);
                    btnCalculate.setEnabled(true);
                });
            }
        });
    }

    private double calculateSubjectCGPA(double percentage) {
        double[] thresholds = {90, 80, 70, 60, 50, 40};
        double[] cgpaValues = {10.0, 9.0, 8.0, 7.0, 6.0, 5.0};
        for (int i = 0; i < thresholds.length; i++) {
            if (percentage >= thresholds[i]) return cgpaValues[i];
        }
        return 0.0;
    }

    private void addSubjectResultView(String subjectName, int credits, double cgpa) {
        TextView tv = new TextView(this);
        tv.setText(String.format("%s (%d credits): %.2f/10", subjectName, credits, cgpa));
        tv.setTextColor(ContextCompat.getColor(this, R.color.primary));
        tv.setTextSize(16);
        tv.setPadding(0, 8, 0, 8);
        subjectResults.addView(tv);
        resultsCard.setVisibility(View.VISIBLE);
    }

    private void displayOverallCGPA(double overallCGPA) {
        tvOverallCgpa.setText(String.format("Overall CGPA: %.2f/10", overallCGPA));
        ValueAnimator animator = ValueAnimator.ofInt(0, (int) (overallCGPA * 10));
        animator.setDuration(1000);
        animator.addUpdateListener(animation -> cgpaProgress.setProgress((int) animation.getAnimatedValue()));
        animator.start();
        resultsCard.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(this, R.color.saffron))
                .setTextColor(ContextCompat.getColor(this, android.R.color.white))
                .show();
        resultsCard.setVisibility(View.GONE);
        cgpaProgress.setVisibility(View.GONE);
        btnCalculate.setEnabled(true);
    }

    private class SubjectsAdapter extends RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder> {

        private Deque<SubjectData> subjects = new ArrayDeque<>();

        @Override
        public SubjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subject_input_advanced, parent, false);
            return new SubjectViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SubjectViewHolder holder, int position) {
            SubjectData subject = subjects.toArray(new SubjectData[0])[position];
            holder.tvSubjectNumber.setText(getString(R.string.subject_number, position + 1));
            holder.etSubjectName.setText(subject.name);
            holder.etTotalMarks.setText(subject.totalMarks > 0 ? String.valueOf(subject.totalMarks) : "");
            holder.etObtainedMarks.setText(subject.obtainedMarks > 0 ? String.valueOf(subject.obtainedMarks) : "");
            holder.sliderCredits.setValue(subject.credits);
            updateMarksProgress(holder, subject);
            holder.btnRemove.setOnClickListener(v -> {
                subjects.remove(subject);
                notifyDataSetChanged();
            });
            holder.etSubjectName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    subject.name = s.toString().trim();
                }
            });
            holder.etTotalMarks.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        subject.totalMarks = s.toString().isEmpty() ? 0 : Double.parseDouble(s.toString());
                        updateMarksProgress(holder, subject);
                    } catch (NumberFormatException e) {
                        subject.totalMarks = 0;
                    }
                }
            });
            holder.etObtainedMarks.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        subject.obtainedMarks = s.toString().isEmpty() ? 0 : Double.parseDouble(s.toString());
                        updateMarksProgress(holder, subject);
                    } catch (NumberFormatException e) {
                        subject.obtainedMarks = 0;
                    }
                }
            });
            holder.sliderCredits.addOnChangeListener((slider, value, fromUser) -> subject.credits = (int) value);
        }

        @Override
        public int getItemCount() {
            return subjects.size();
        }

        void addSubject() {
            subjects.add(new SubjectData("", 3, 0, 0));
            notifyItemInserted(subjects.size() - 1);
        }

        Deque<SubjectData> getValidSubjects() {
            Deque<SubjectData> validSubjects = new ArrayDeque<>();
            for (SubjectData subject : subjects) {
                if (!subject.name.isEmpty() && subject.totalMarks > 0 && subject.obtainedMarks >= 0 && subject.obtainedMarks <= subject.totalMarks) {
                    validSubjects.add(subject);
                }
            }
            return validSubjects;
        }

        private void updateMarksProgress(SubjectViewHolder holder, SubjectData subject) {
            if (subject.totalMarks <= 0 || subject.obtainedMarks < 0) {
                holder.marksProgress.setProgress(0);
                return;
            }
            int progress = (int) ((subject.obtainedMarks / subject.totalMarks) * 100);
            holder.marksProgress.setProgress(Math.min(progress, 100));
        }

        class SubjectViewHolder extends RecyclerView.ViewHolder {
            TextInputEditText etSubjectName;
            TextInputEditText etTotalMarks;
            TextInputEditText etObtainedMarks;
            com.google.android.material.slider.Slider sliderCredits;
            MaterialButton btnRemove;
            TextView tvSubjectNumber;
            com.google.android.material.progressindicator.LinearProgressIndicator marksProgress;

            SubjectViewHolder(View itemView) {
                super(itemView);
                etSubjectName = itemView.findViewById(R.id.et_subject_name);
                etTotalMarks = itemView.findViewById(R.id.et_total_marks);
                etObtainedMarks = itemView.findViewById(R.id.et_obtained_marks);
                sliderCredits = itemView.findViewById(R.id.slider_credits);
                btnRemove = itemView.findViewById(R.id.btn_remove_subject);
                tvSubjectNumber = itemView.findViewById(R.id.tv_subject_number);
                marksProgress = itemView.findViewById(R.id.marks_progress);
            }
        }
    }

    private static class SubjectData {
        String name;
        int credits;
        double totalMarks;
        double obtainedMarks;

        SubjectData(String name, int credits, double totalMarks, double obtainedMarks) {
            this.name = name;
            this.credits = credits;
            this.totalMarks = totalMarks;
            this.obtainedMarks = obtainedMarks;
        }
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}