package com.example.abcd.ExamQuizes;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.abcd.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateNewQuizes extends AppCompatActivity {
    // UI Components and Variables
    private Spinner semesterSpinner, subjectSpinner;
    private Map<String, List<String>> semesterSubjectsMap = new HashMap<>();
    private CardView optionsCardView;
    private Button btnExistingQuizzes, btnAddNew;
    private ProgressBar cardProgressBar;
    private DatabaseReference quizzesRef;
    private EditText dateEditText, timeEditText, durationEditText;
    private TextView numberOfQuestionsTextView, savedQuestionsCountTextView;
    private Spinner resultViewSpinner;
    private Button saveButton;
    private TextInputLayout quizSizeInputLayout;
    private EditText quizSizeEditText;
    private Button confirmQuizSizeButton;

    // New UI elements for marks configuration
    private TextView marksPerQuestionValue, negativeMarkingValue;
    private Button marksPerQuestionUpButton, marksPerQuestionDownButton;
    private Button negativeMarkingUpButton, negativeMarkingDownButton;

    private ArrayList<Question> questionsList = new ArrayList<>();
    private SimpleDateFormat dateFormatter, timeFormatter;
    private Calendar calendar = Calendar.getInstance();
    private DatabaseReference databaseReference;
    private ActivityResultLauncher<Intent> addQuestionsLauncher;

    // Variables to store marks values
    private float marksPerQuestion = 1.0f;
    private float negativeMarking = 0.0f;
    private static final float MARKS_STEP = 0.25f;
    private static final float MIN_MARKS = 0.0f;
    private static final float MAX_MARKS = 10.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_new_quizes);

        databaseReference = FirebaseDatabase.getInstance().getReference("quizzes");

        setupWindowInsets();
        initializeViews();
        setupDateTimeFormatters();
        setupSpinner();
        setupSemesterSubjectSystem();
        setupActivityResultLauncher();
        setupClickListeners();
        updateMarksDisplay();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        optionsCardView = findViewById(R.id.optionsCardView);
        btnExistingQuizzes = findViewById(R.id.btnExistingQuizzes);
        btnAddNew = findViewById(R.id.btnAddNew);
        cardProgressBar = findViewById(R.id.cardProgressBar);
        semesterSpinner = findViewById(R.id.semesterSpinner);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        dateEditText = findViewById(R.id.dateEditText);
        timeEditText = findViewById(R.id.timeEditText);
        durationEditText = findViewById(R.id.durationEditText);
        resultViewSpinner = findViewById(R.id.resultViewSpinner);
        savedQuestionsCountTextView = findViewById(R.id.savedQuestionsCountTextView);
        saveButton = findViewById(R.id.saveButton);
        numberOfQuestionsTextView = findViewById(R.id.numberOfQuestionsTextView);
        quizzesRef = FirebaseDatabase.getInstance().getReference("quizzes");
        quizSizeInputLayout = findViewById(R.id.quizSizeInputLayout);
        quizSizeEditText = findViewById(R.id.quizSizeEditText);
        confirmQuizSizeButton = findViewById(R.id.confirmQuizSizeButton);

        marksPerQuestionValue = findViewById(R.id.marksPerQuestionValue);
        negativeMarkingValue = findViewById(R.id.negativeMarkingValue);
        marksPerQuestionUpButton = findViewById(R.id.marksPerQuestionUpButton);
        marksPerQuestionDownButton = findViewById(R.id.marksPerQuestionDownButton);
        negativeMarkingUpButton = findViewById(R.id.negativeMarkingUpButton);
        negativeMarkingDownButton = findViewById(R.id.negativeMarkingDownButton);
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

    private void setupSemesterSubjectSystem() {
        DatabaseReference subjectsRef = FirebaseDatabase.getInstance().getReference("subjects");
        subjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> semesters = new ArrayList<>();
                for (DataSnapshot semesterSnapshot : snapshot.getChildren()) {
                    String semesterKey = semesterSnapshot.getKey();
                    semesters.add(semesterKey.replace("_", " ").toUpperCase());
                    List<String> subjects = new ArrayList<>();
                    for (DataSnapshot subjectSnapshot : semesterSnapshot.getChildren()) {
                        subjects.add(subjectSnapshot.getValue(String.class));
                    }
                    semesterSubjectsMap.put(semesterKey, subjects);
                }
                setupSemesterSpinner(semesters);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateNewQuizes.this,
                        "Failed to load subjects", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSemesterSpinner(List<String> semesters) {
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, semesters);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        semesterSpinner.setAdapter(semesterAdapter);

        semesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSemesterKey = semesters.get(position).replace(" ", "_").toLowerCase();
                setupSubjectSpinner(semesterSubjectsMap.get(selectedSemesterKey));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                subjectSpinner.setAdapter(null);
            }
        });
    }

    private void setupSubjectSpinner(List<String> subjects) {
        if (subjects == null || subjects.isEmpty()) {
            subjectSpinner.setAdapter(null);
            return;
        }
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, subjects);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectAdapter);
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
        numberOfQuestionsTextView.setOnClickListener(v -> toggleCardView(true));

        btnAddNew.setOnClickListener(v -> {
            openAddQuestionActivity();
            toggleCardView(false);
        });

        btnExistingQuizzes.setOnClickListener(v -> {
            quizSizeEditText.setText("");
            btnExistingQuizzes.setVisibility(View.GONE);
            btnAddNew.setVisibility(View.GONE);
            quizSizeInputLayout.setVisibility(View.VISIBLE);
            confirmQuizSizeButton.setVisibility(View.VISIBLE);
        });

        confirmQuizSizeButton.setOnClickListener(v -> {
            String input = quizSizeEditText.getText().toString().trim();
            if (TextUtils.isEmpty(input)) {
                quizSizeEditText.setError("Please enter number of questions");
                Toast.makeText(this, "Please enter a value", Toast.LENGTH_SHORT).show();
                return;
            }

            int userInput;
            try {
                userInput = Integer.parseInt(input);
                if (userInput <= 0) {
                    quizSizeEditText.setError("Enter a number greater than 0");
                    return;
                }
            } catch (NumberFormatException e) {
                quizSizeEditText.setError("Invalid number");
                return;
            }

            String subject = subjectSpinner.getSelectedItem() != null ?
                    subjectSpinner.getSelectedItem().toString() : "";
            if (subject.isEmpty()) {
                Toast.makeText(this, "Please select a subject first", Toast.LENGTH_SHORT).show();
                return;
            }

            cardProgressBar.setVisibility(View.VISIBLE);
            loadExistingQuestionsWithLimit(userInput);
        });

        dateEditText.setOnClickListener(v -> showDatePicker());
        timeEditText.setOnClickListener(v -> showTimePicker());
        saveButton.setOnClickListener(v -> validateAndSaveQuiz());

        marksPerQuestionUpButton.setOnClickListener(v -> {
            if (marksPerQuestion < MAX_MARKS) {
                marksPerQuestion += MARKS_STEP;
                updateMarksDisplay();
            }
        });
        marksPerQuestionDownButton.setOnClickListener(v -> {
            if (marksPerQuestion > MIN_MARKS) {
                marksPerQuestion -= MARKS_STEP;
                updateMarksDisplay();
            }
        });

        negativeMarkingUpButton.setOnClickListener(v -> {
            if (negativeMarking < MAX_MARKS) {
                negativeMarking += MARKS_STEP;
                updateMarksDisplay();
            }
        });
        negativeMarkingDownButton.setOnClickListener(v -> {
            if (negativeMarking > MIN_MARKS) {
                negativeMarking -= MARKS_STEP;
                updateMarksDisplay();
            }
        });
    }

    private void toggleCardView(boolean show) {
        if (show) {
            optionsCardView.setVisibility(View.VISIBLE);
            btnExistingQuizzes.setVisibility(View.VISIBLE);
            btnAddNew.setVisibility(View.VISIBLE);
            quizSizeInputLayout.setVisibility(View.GONE);
            confirmQuizSizeButton.setVisibility(View.GONE);
            cardProgressBar.setVisibility(View.GONE);
        } else {
            optionsCardView.setVisibility(View.GONE);
        }
    }

    private void loadExistingQuestionsWithLimit(int requestedLimit) {
        String subject = subjectSpinner.getSelectedItem() != null ?
                subjectSpinner.getSelectedItem().toString() : "";
        if (subject.isEmpty()) {
            Toast.makeText(this, "Subject not selected", Toast.LENGTH_SHORT).show();
            cardProgressBar.setVisibility(View.GONE);
            return;
        }

        DatabaseReference questionBankRef = FirebaseDatabase.getInstance()
                .getReference("Questionbank")
                .child(subject);

        questionBankRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Question> availableQuestions = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String questionText = child.getKey();
                    Map<String, String> optionsMap = new HashMap<>();
                    int correctAnswer = -1;

                    if (child.getValue() instanceof Map) {
                        Map map = (Map) child.getValue();
                        if (map.containsKey("options") && map.get("options") instanceof Map) {
                            Map optionsData = (Map) map.get("options");
                            for (Object key : optionsData.keySet()) {
                                optionsMap.put(key.toString(), optionsData.get(key).toString());
                            }
                        }
                        if (map.containsKey("correctAnswer")) {
                            try {
                                correctAnswer = Integer.parseInt(map.get("correctAnswer").toString());
                            } catch (Exception e) {
                                correctAnswer = -1;
                            }
                        }
                    }
                    Question q = new Question(questionText, optionsMap, correctAnswer);
                    availableQuestions.add(q);
                }

                int totalAvailable = availableQuestions.size();
                int finalLimit = requestedLimit; // Use a local variable to adjust limit

                if (totalAvailable == 0) {
                    Toast.makeText(CreateNewQuizes.this,
                            "No questions available for this subject",
                            Toast.LENGTH_SHORT).show();
                } else if (requestedLimit > totalAvailable) {
                    Toast.makeText(CreateNewQuizes.this,
                            "Requested " + requestedLimit + " questions, but only " + totalAvailable + " available",
                            Toast.LENGTH_SHORT).show();
                    finalLimit = totalAvailable;
                }

                Collections.shuffle(availableQuestions);
                // Ensure we don't try to take more than available
                questionsList = new ArrayList<>(availableQuestions.subList(0, Math.min(finalLimit, totalAvailable)));
                updateQuestionCount();
                cardProgressBar.setVisibility(View.GONE);
                toggleCardView(false);
                Toast.makeText(CreateNewQuizes.this,
                        "Loaded " + questionsList.size() + " questions",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateNewQuizes.this,
                        "Failed to load questions: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                cardProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void openAddQuestionActivity() {
        Intent intent = new Intent(this, addQuestionActivity.class);
        String selectedSubject = subjectSpinner.getSelectedItem() != null ?
                subjectSpinner.getSelectedItem().toString() : "";
        String selectedSemester = semesterSpinner.getSelectedItem() != null ?
                semesterSpinner.getSelectedItem().toString() : "";
        intent.putExtra("subject_name", selectedSubject);
        intent.putExtra("semester", selectedSemester);
        addQuestionsLauncher.launch(intent);
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // If today's date is selected, check and possibly update the time
            Calendar today = Calendar.getInstance();
            if (year == today.get(Calendar.YEAR) && month == today.get(Calendar.MONTH) && dayOfMonth == today.get(Calendar.DAY_OF_MONTH)) {
                // If we have a time already set, verify it's not in the past
                if (!TextUtils.isEmpty(timeEditText.getText())) {
                    try {
                        Date selectedTime = timeFormatter.parse(timeEditText.getText().toString());
                        Calendar selectedTimeCal = Calendar.getInstance();
                        selectedTimeCal.setTime(selectedTime);

                        Calendar currentTime = Calendar.getInstance();
                        if (selectedTimeCal.get(Calendar.HOUR_OF_DAY) < currentTime.get(Calendar.HOUR_OF_DAY) ||
                                (selectedTimeCal.get(Calendar.HOUR_OF_DAY) == currentTime.get(Calendar.HOUR_OF_DAY) &&
                                        selectedTimeCal.get(Calendar.MINUTE) < currentTime.get(Calendar.MINUTE))) {
                            // Reset the time field as it's now invalid
                            timeEditText.setText("");
                            Toast.makeText(CreateNewQuizes.this, "Please select a future time", Toast.LENGTH_SHORT).show();
                        }
                    } catch (ParseException e) {
                        timeEditText.setText("");
                    }
                }
            }

            dateEditText.setText(dateFormatter.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        // Set the minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar currentTime = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            // Check if the selected date is today
            Calendar today = Calendar.getInstance();
            boolean isToday = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);

            // If today is selected, verify the time is not in the past
            if (isToday) {
                if (hourOfDay < currentTime.get(Calendar.HOUR_OF_DAY) ||
                        (hourOfDay == currentTime.get(Calendar.HOUR_OF_DAY) && minute <= currentTime.get(Calendar.MINUTE))) {
                    Toast.makeText(CreateNewQuizes.this, "Please select a future time", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            timeEditText.setText(timeFormatter.format(calendar.getTime()));
        }, currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE), true).show();
    }

    private void updateQuestionCount() {
        int count = questionsList != null ? questionsList.size() : 0;
        savedQuestionsCountTextView.setText(String.format("Saved Questions: %d", count));
    }

    private void updateMarksDisplay() {
        marksPerQuestionValue.setText(String.format("%.2f", marksPerQuestion));
        negativeMarkingValue.setText(String.format("%.2f", negativeMarking));

        marksPerQuestionUpButton.setEnabled(marksPerQuestion < MAX_MARKS);
        marksPerQuestionDownButton.setEnabled(marksPerQuestion > MIN_MARKS);
        negativeMarkingUpButton.setEnabled(negativeMarking < MAX_MARKS);
        negativeMarkingDownButton.setEnabled(negativeMarking > MIN_MARKS);
    }

    private void validateAndSaveQuiz() {
        if (!validateInputs()) return;

        try {
            String subject = subjectSpinner.getSelectedItem().toString();
            Date date = dateFormatter.parse(dateEditText.getText().toString());
            Date time = timeFormatter.parse(timeEditText.getText().toString());

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

            Quiz quiz = new Quiz(
                    subject,
                    combinedCal.getTimeInMillis(),
                    Integer.parseInt(durationEditText.getText().toString()),
                    marksPerQuestion,
                    negativeMarking,
                    questionsList,
                    resultViewSpinner.getSelectedItem().toString()
            );

            String quizId = databaseReference.push().getKey();
            quiz.setQuizId(quizId);

            databaseReference.child(quizId).setValue(quiz.toMap(), (error, ref) -> {
                if (error == null) {
                    showToast("Quiz saved successfully! ID: " + quizId);
                    finish();
                } else {
                    showToast("Failed to save quiz: " + error.getMessage());
                }
            });

        } catch (ParseException e) {
            showToast("Invalid date/time format: " + e.getMessage());
        } catch (NumberFormatException e) {
            showToast("Invalid number format in duration");
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN &&
                optionsCardView.getVisibility() == View.VISIBLE) {
            Rect outRect = new Rect();
            optionsCardView.getGlobalVisibleRect(outRect);
            if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                toggleCardView(false);
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Existing validation code...

        // Add validation for date and time
        try {
            String dateStr = dateEditText.getText().toString();
            String timeStr = timeEditText.getText().toString();

            if (!TextUtils.isEmpty(dateStr) && !TextUtils.isEmpty(timeStr)) {
                Date date = dateFormatter.parse(dateStr);
                Date time = timeFormatter.parse(timeStr);

                Calendar dateCal = Calendar.getInstance();
                dateCal.setTime(date);

                Calendar timeCal = Calendar.getInstance();
                timeCal.setTime(time);

                Calendar scheduledTime = Calendar.getInstance();
                scheduledTime.set(
                        dateCal.get(Calendar.YEAR),
                        dateCal.get(Calendar.MONTH),
                        dateCal.get(Calendar.DAY_OF_MONTH),
                        timeCal.get(Calendar.HOUR_OF_DAY),
                        timeCal.get(Calendar.MINUTE)
                );

                Calendar now = Calendar.getInstance();
                if (scheduledTime.before(now)) {
                    Toast.makeText(this, "Quiz cannot be scheduled in the past", Toast.LENGTH_SHORT).show();
                    isValid = false;
                }
            }
        } catch (ParseException e) {
            // Error will be caught by other validations
        }

        return isValid;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}