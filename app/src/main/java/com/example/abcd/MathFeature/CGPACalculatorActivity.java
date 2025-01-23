package com.example.abcd.MathFeature;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.abcd.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class CGPACalculatorActivity extends AppCompatActivity {

    private LinearLayout inputContainer, subjectResults;
    private MaterialCardView resultsCard;
    private CircularProgressIndicator cgpaProgress;
    private List<SubjectInput> subjectInputs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cgpacalculator);

        initializeViews();
        setupButtonClickListeners();
    }

    private void initializeViews() {
        inputContainer = findViewById(R.id.input_container);
        subjectResults = findViewById(R.id.subject_results);
        resultsCard = findViewById(R.id.results_card);
        cgpaProgress = findViewById(R.id.cgpa_progress);
        cgpaProgress.setMax(100);
    }

    private void setupButtonClickListeners() {
        findViewById(R.id.btn_add_subject).setOnClickListener(v -> addSubjectInput());
        findViewById(R.id.btn_calculate).setOnClickListener(v -> calculateCGPA());
    }

    private void addSubjectInput() {
        MaterialCardView subjectCard = (MaterialCardView) getLayoutInflater().inflate(R.layout.subject_input, null);
        inputContainer.addView(subjectCard);

        SubjectInput subjectInput = new SubjectInput(
                subjectCard.findViewById(R.id.et_subject_name),
                subjectCard.findViewById(R.id.slider_credits),
                subjectCard.findViewById(R.id.et_total_marks),
                subjectCard.findViewById(R.id.et_obtained_marks),
                subjectCard.findViewById(R.id.btn_remove_subject),
                subjectCard.findViewById(R.id.tv_subject_number),
                subjectCard.findViewById(R.id.marks_progress),
                subjectCard
        );

        int subjectNumber = subjectInputs.size() + 1;
        subjectInput.tvSubjectNumber.setText(getString(R.string.subject_number, subjectNumber));

        setupRemoveButton(subjectInput);
        setupCreditsSlider(subjectInput);
        setupMarksWatchers(subjectInput);

        subjectInputs.add(subjectInput);
    }

    private void setupRemoveButton(SubjectInput subjectInput) {
        subjectInput.btnRemove.setOnClickListener(v -> {
            inputContainer.removeView(subjectInput.cardView);
            subjectInputs.remove(subjectInput);
            updateSubjectNumbers();
        });
    }

    private void setupCreditsSlider(SubjectInput subjectInput) {
        subjectInput.sliderCredits.addOnChangeListener((slider, value, fromUser) -> {
            // You can add additional credit-related logic here if needed
        });
    }

    private void setupMarksWatchers(SubjectInput subjectInput) {
        TextWatcher marksWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateMarksProgress(subjectInput);
            }
        };

        subjectInput.etTotalMarks.addTextChangedListener(marksWatcher);
        subjectInput.etObtainedMarks.addTextChangedListener(marksWatcher);
    }

    private void updateMarksProgress(SubjectInput subjectInput) {
        try {
            String totalStr = subjectInput.etTotalMarks.getText().toString().trim();
            String obtainedStr = subjectInput.etObtainedMarks.getText().toString().trim();

            if (totalStr.isEmpty() || obtainedStr.isEmpty()) {
                subjectInput.marksProgress.setProgress(0);
                return;
            }

            double total = Double.parseDouble(totalStr);
            double obtained = Double.parseDouble(obtainedStr);

            if (total <= 0) {
                subjectInput.marksProgress.setProgress(0);
                return;
            }

            int progress = (int) ((obtained / total) * 100);
            subjectInput.marksProgress.setProgress(Math.min(progress, 100));
        } catch (NumberFormatException e) {
            subjectInput.marksProgress.setProgress(0);
        }
    }

    private void updateSubjectNumbers() {
        for (int i = 0; i < subjectInputs.size(); i++) {
            SubjectInput input = subjectInputs.get(i);
            input.tvSubjectNumber.setText(getString(R.string.subject_number, i + 1));
        }
    }

    private void calculateCGPA() {
        try {
            resultsCard.setVisibility(View.VISIBLE);
            subjectResults.removeAllViews();
            cgpaProgress.setVisibility(View.VISIBLE);

            double totalWeightedPoints = 0;
            int totalCredits = 0;
            int validSubjects = 0;

            for (SubjectInput input : subjectInputs) {
                String subjectName = input.etSubjectName.getText().toString().trim();
                int credits = (int) input.sliderCredits.getValue();
                String totalStr = input.etTotalMarks.getText().toString().trim();
                String obtainedStr = input.etObtainedMarks.getText().toString().trim();

                if (subjectName.isEmpty() || totalStr.isEmpty() || obtainedStr.isEmpty()) continue;

                double totalMarks = Double.parseDouble(totalStr);
                double obtainedMarks = Double.parseDouble(obtainedStr);

                if (totalMarks <= 0) {
                    throw new IllegalArgumentException("Total marks must be greater than 0");
                }
                if (obtainedMarks < 0 || obtainedMarks > totalMarks) {
                    throw new IllegalArgumentException("Obtained marks must be 0-" + totalMarks);
                }

                double percentage = (obtainedMarks / totalMarks) * 100;
                double subjectCGPA = calculateSubjectCGPA(percentage);

                addSubjectResultView(subjectName, credits, subjectCGPA);
                totalWeightedPoints += subjectCGPA * credits;
                totalCredits += credits;
                validSubjects++;
            }

            if (validSubjects == 0) {
                showError("Please fill at least one complete subject");
                return;
            }

            double overallCGPA = totalWeightedPoints / totalCredits;
            displayOverallCGPA(overallCGPA);

        } catch (NumberFormatException e) {
            showError("Please enter valid numbers in all fields");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } finally {
            cgpaProgress.setVisibility(View.GONE);
        }
    }

    private double calculateSubjectCGPA(double percentage) {
        if (percentage >= 90) return 10.0;
        if (percentage >= 80) return 9.0;
        if (percentage >= 70) return 8.0;
        if (percentage >= 60) return 7.0;
        if (percentage >= 50) return 6.0;
        if (percentage >= 40) return 5.0;
        return 0.0;
    }

    private void addSubjectResultView(String subjectName, int credits, double cgpa) {
        TextView tv = new TextView(this);
        tv.setText(String.format("%s (%d credits): %.2f/10", subjectName, credits, cgpa));
        tv.setTextColor(ContextCompat.getColor(this, R.color.primary));
        tv.setTextSize(16);
        tv.setPadding(0, 8, 0, 8);
        subjectResults.addView(tv);
    }

    private void displayOverallCGPA(double overallCGPA) {
        TextView tvOverall = findViewById(R.id.tv_overall_cgpa);
        tvOverall.setText(String.format("Overall CGPA: %.2f/10", overallCGPA));
        tvOverall.setTextColor(ContextCompat.getColor(this, R.color.primary));
        cgpaProgress.setProgress((int) (overallCGPA * 10));
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(this, R.color.saffron))
                .setTextColor(Color.WHITE)
                .show();
    }

    private static class SubjectInput {
        final TextInputEditText etSubjectName;
        final Slider sliderCredits;
        final TextInputEditText etTotalMarks;
        final TextInputEditText etObtainedMarks;
        final MaterialButton btnRemove;
        final TextView tvSubjectNumber;
        final LinearProgressIndicator marksProgress;
        final MaterialCardView cardView;

        SubjectInput(TextInputEditText etSubjectName,
                     Slider sliderCredits,
                     TextInputEditText etTotalMarks,
                     TextInputEditText etObtainedMarks,
                     MaterialButton btnRemove,
                     TextView tvSubjectNumber,
                     LinearProgressIndicator marksProgress,
                     MaterialCardView cardView) {
            this.etSubjectName = etSubjectName;
            this.sliderCredits = sliderCredits;
            this.etTotalMarks = etTotalMarks;
            this.etObtainedMarks = etObtainedMarks;
            this.btnRemove = btnRemove;
            this.tvSubjectNumber = tvSubjectNumber;
            this.marksProgress = marksProgress;
            this.cardView = cardView;
        }
    }
}