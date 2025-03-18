package com.example.abcd.ExamQuizes;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.abcd.R;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateNewQuizes extends AppCompatActivity {

    // UI Components
    private EditText subjectEditText, marksPerQuestionEditText, negativeMarkingEditText;
    private EditText dateEditText, timeEditText, durationEditText;
    private TextView numberOfQuestionsTextView, savedQuestionsCountTextView;
    private Spinner resultViewSpinner;
    private Button saveButton;

    // Data Handling
    private ArrayList<QuizQuestion> questionsList = new ArrayList<>();
    private SimpleDateFormat dateFormatter, timeFormatter;
    private Calendar calendar = Calendar.getInstance();

    // Firebase
    private DatabaseReference databaseReference;
    private ActivityResultLauncher<Intent> addQuestionsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_new_quizes);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("quizzes");

        setupWindowInsets();
        initializeViews();
        setupDateTimeFormatters();
        setupSpinner();
        setupActivityResultLauncher();
        setupClickListeners();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        subjectEditText = findViewById(R.id.subjectEditText);
        marksPerQuestionEditText = findViewById(R.id.marksPerQuestionEditText);
        negativeMarkingEditText = findViewById(R.id.negativeMarkingEditText);
        dateEditText = findViewById(R.id.dateEditText);
        timeEditText = findViewById(R.id.timeEditText);
        durationEditText = findViewById(R.id.durationEditText);
        resultViewSpinner = findViewById(R.id.resultViewSpinner);
        savedQuestionsCountTextView = findViewById(R.id.savedQuestionsCountTextView);
        saveButton = findViewById(R.id.saveButton);
        numberOfQuestionsTextView = findViewById(R.id.numberOfQuestionsTextView);
    }

    private void setupDateTimeFormatters() {
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.result_view_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resultViewSpinner.setAdapter(adapter);
    }

    private void setupActivityResultLauncher() {
        addQuestionsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        questionsList = result.getData().getParcelableArrayListExtra("questions");
                        updateQuestionCount();
                    }
                });
    }

    private void setupClickListeners() {
        numberOfQuestionsTextView.setOnClickListener(v -> openAddQuestionActivity());
        dateEditText.setOnClickListener(v -> showDatePicker());
        timeEditText.setOnClickListener(v -> showTimePicker());
        saveButton.setOnClickListener(v -> validateAndSaveQuiz());
    }

    private void openAddQuestionActivity() {
        Intent intent = new Intent(this, addQuestionActivity.class);
        addQuestionsLauncher.launch(intent);
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            dateEditText.setText(dateFormatter.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            timeEditText.setText(timeFormatter.format(calendar.getTime()));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
                .show();
    }

    private void updateQuestionCount() {
        int count = questionsList != null ? questionsList.size() : 0;
        savedQuestionsCountTextView.setText(String.format("Saved Questions: %d", count));
    }

    private void validateAndSaveQuiz() {
        if (!validateInputs()) return;

        try {
            // Parse date and time
            Date date = dateFormatter.parse(dateEditText.getText().toString());
            Date time = timeFormatter.parse(timeEditText.getText().toString());

            // Combine date and time
            Calendar dateCal = Calendar.getInstance();
            dateCal.setTime(date);
            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime(time);

            Calendar combinedCal = Calendar.getInstance();
            combinedCal.set(dateCal.get(Calendar.YEAR),
                    dateCal.get(Calendar.MONTH),
                    dateCal.get(Calendar.DAY_OF_MONTH),
                    timeCal.get(Calendar.HOUR_OF_DAY),
                    timeCal.get(Calendar.MINUTE));

            // Create quiz object
            Quiz quiz = new Quiz(
                    subjectEditText.getText().toString(),
                    combinedCal.getTimeInMillis(),
                    Integer.parseInt(durationEditText.getText().toString()),
                    Float.parseFloat(marksPerQuestionEditText.getText().toString()),
                    Float.parseFloat(negativeMarkingEditText.getText().toString()),
                    questionsList,
                    resultViewSpinner.getSelectedItem().toString()
            );

            // Save to Firebase
            String quizId = databaseReference.push().getKey();
            databaseReference.child(quizId).setValue(quiz.toMap(), new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError error, DatabaseReference ref) {
                    if (error == null) {
                        showToast("Quiz saved successfully! ID: " + quizId);
                        finish();
                    } else {
                        showToast("Failed to save quiz: " + error.getMessage());
                    }
                }
            });

        } catch (ParseException e) {
            showToast("Invalid date/time format: " + e.getMessage());
        } catch (NumberFormatException e) {
            showToast("Invalid number format in marks/duration");
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (TextUtils.isEmpty(subjectEditText.getText())) {
            subjectEditText.setError("Subject required");
            isValid = false;
        }
        if (TextUtils.isEmpty(marksPerQuestionEditText.getText())) {
            marksPerQuestionEditText.setError("Marks per question required");
            isValid = false;
        }
        if (TextUtils.isEmpty(negativeMarkingEditText.getText())) {
            negativeMarkingEditText.setError("Negative marking required");
            isValid = false;
        }
        if (TextUtils.isEmpty(dateEditText.getText())) {
            dateEditText.setError("Date required");
            isValid = false;
        }
        if (TextUtils.isEmpty(timeEditText.getText())) {
            timeEditText.setError("Time required");
            isValid = false;
        }
        if (TextUtils.isEmpty(durationEditText.getText())) {
            durationEditText.setError("Duration required");
            isValid = false;
        }
        if (questionsList == null || questionsList.isEmpty()) {
            showToast("Add at least one question");
            isValid = false;
        }

        return isValid;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}