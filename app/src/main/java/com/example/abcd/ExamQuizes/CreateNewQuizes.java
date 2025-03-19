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

    private EditText marksPerQuestionEditText, negativeMarkingEditText;
    private EditText dateEditText, timeEditText, durationEditText;
    private TextView numberOfQuestionsTextView, savedQuestionsCountTextView;
    private Spinner resultViewSpinner;
    private Button saveButton;

    // New UI components for quiz size input inside card view
    private EditText quizSizeEditText;
    private Button confirmQuizSizeButton;

    private ArrayList<Question> questionsList = new ArrayList<Question>();
    private SimpleDateFormat dateFormatter, timeFormatter;
    private Calendar calendar = Calendar.getInstance();

    // Firebase database reference for quizzes
    private DatabaseReference databaseReference;
    private ActivityResultLauncher<Intent> addQuestionsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_new_quizes);

        // Initialize Firebase reference for quizzes
        databaseReference = FirebaseDatabase.getInstance().getReference("quizzes");

        setupWindowInsets();
        initializeViews();
        setupDateTimeFormatters();
        setupSpinner();
        setupSemesterSubjectSystem();
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
        optionsCardView = findViewById(R.id.optionsCardView);
        btnExistingQuizzes = findViewById(R.id.btnExistingQuizzes);
        btnAddNew = findViewById(R.id.btnAddNew);
        cardProgressBar = findViewById(R.id.cardProgressBar);
        semesterSpinner = findViewById(R.id.semesterSpinner);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        marksPerQuestionEditText = findViewById(R.id.marksPerQuestionEditText);
        negativeMarkingEditText = findViewById(R.id.negativeMarkingEditText);
        dateEditText = findViewById(R.id.dateEditText);
        timeEditText = findViewById(R.id.timeEditText);
        durationEditText = findViewById(R.id.durationEditText);
        resultViewSpinner = findViewById(R.id.resultViewSpinner);
        savedQuestionsCountTextView = findViewById(R.id.savedQuestionsCountTextView);
        saveButton = findViewById(R.id.saveButton);
        numberOfQuestionsTextView = findViewById(R.id.numberOfQuestionsTextView);
        quizzesRef = FirebaseDatabase.getInstance().getReference("quizzes");

        // Initialize new UI elements for quiz size input
        quizSizeEditText = findViewById(R.id.quizSizeEditText);
        confirmQuizSizeButton = findViewById(R.id.confirmQuizSizeButton);
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
        numberOfQuestionsTextView.setOnClickListener(v -> toggleCardView());

        // For "Insert into Database" button, simply launch the addQuestionActivity
        btnAddNew.setOnClickListener(v -> {
            openAddQuestionActivity();
            optionsCardView.setVisibility(View.GONE);
        });

        // Modified logic for "Get From Available Quizzes" (button 1)
        btnExistingQuizzes.setOnClickListener(v -> {
            // Show the quiz size input UI and hide the two option buttons
            btnExistingQuizzes.setVisibility(View.GONE);
            btnAddNew.setVisibility(View.GONE);
            quizSizeEditText.setVisibility(View.VISIBLE);
            confirmQuizSizeButton.setVisibility(View.VISIBLE);
        });

        // Confirm button for quiz size input
        confirmQuizSizeButton.setOnClickListener(v -> {
            String input = quizSizeEditText.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(CreateNewQuizes.this, "Please enter a value", Toast.LENGTH_SHORT).show();
                return;
            }
            int userInput = Integer.parseInt(input);
            String subject = subjectSpinner.getSelectedItem() != null ? subjectSpinner.getSelectedItem().toString() : "";
            if (subject.isEmpty()) {
                Toast.makeText(CreateNewQuizes.this, "Please select a subject", Toast.LENGTH_SHORT).show();
                return;
            }
            // Query Firebase "Questionbank" to check available questions for the subject
            DatabaseReference questionBankRef = FirebaseDatabase.getInstance().getReference("Questionbank").child(subject);
            questionBankRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int totalAvailable = (int) snapshot.getChildrenCount();
                    int finalQuizSize = userInput;
                    if (userInput > totalAvailable) {
                        finalQuizSize = totalAvailable;
                        Toast.makeText(CreateNewQuizes.this,
                                "Input exceeds available questions; using " + totalAvailable, Toast.LENGTH_SHORT).show();
                    }
                    // Load available questions from Questionbank with the determined limit
                    loadExistingQuestionsWithLimit(finalQuizSize);
                    // Optionally, reset the card view UI
                    quizSizeEditText.setVisibility(View.GONE);
                    confirmQuizSizeButton.setVisibility(View.GONE);
                    // (If needed, you can also restore the two buttons' visibility for later use)
                    btnExistingQuizzes.setVisibility(View.VISIBLE);
                    btnAddNew.setVisibility(View.VISIBLE);
                    optionsCardView.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(CreateNewQuizes.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        dateEditText.setOnClickListener(v -> showDatePicker());
        timeEditText.setOnClickListener(v -> showTimePicker());
        saveButton.setOnClickListener(v -> validateAndSaveQuiz());
    }

    // (Optional) Existing method stub – you can add code if needed.
    private void loadExistingQuestions() {
        // Not used in the new flow
    }

    private void toggleCardView() {
        if (optionsCardView.getVisibility() == View.VISIBLE) {
            optionsCardView.setVisibility(View.GONE);
        } else {
            optionsCardView.setVisibility(View.VISIBLE);
            // Optionally, you can load existing questions count here as before.
        }
    }

    // New method to load questions from Questionbank with a limit
    private void loadExistingQuestionsWithLimit(int limit) {
        String subject = subjectSpinner.getSelectedItem() != null ? subjectSpinner.getSelectedItem().toString() : "";
        if (subject.isEmpty()) {
            Toast.makeText(this, "Subject not selected", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference questionBankRef = FirebaseDatabase.getInstance().getReference("Questionbank").child(subject);
        questionBankRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Question> availableQuestions = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String questionText = child.getKey();
                    Map<String, String> optionsMap = new HashMap<>();
                    int correctAnswer = -1;

                    if (child.exists() && child.getValue() instanceof Map) {
                        Map map = (Map) child.getValue();
                        // Collect options
                        if (map.containsKey("options")) {
                            Object optionsObj = map.get("options");
                            if (optionsObj instanceof Map) {
                                Map optionsData = (Map) optionsObj;
                                // Iterate over options (assuming keys like "option1", "option2", etc.)
                                for (Object key : optionsData.keySet()) {
                                    optionsMap.put(key.toString(), optionsData.get(key).toString());
                                }
                            }
                        }
                        // Parse correct answer
                        if (map.containsKey("correctAnswer")) {
                            try {
                                correctAnswer = Integer.parseInt(map.get("correctAnswer").toString());
                            } catch (Exception e) {
                                correctAnswer = -1;
                            }
                        }
                    }
                    // Create a Question object (ensure your Quiz and Question classes are aligned)
                    Question q = new Question(questionText, optionsMap, correctAnswer);
                    availableQuestions.add(q);
                }
                // Randomize the available questions
                Collections.shuffle(availableQuestions);
                // Limit the list to the user-specified size
                if (availableQuestions.size() > limit) {
                    availableQuestions = new ArrayList<>(availableQuestions.subList(0, limit));
                }
                // Update your questions list
                // Note: Change the type of questionsList to ArrayList<Question> or convert accordingly
                questionsList = availableQuestions;
                updateQuestionCount();
                Toast.makeText(CreateNewQuizes.this, "Loaded " + questionsList.size() + " questions", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateNewQuizes.this, "Failed to load questions: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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

            // Log or Toast the values for debugging if needed
            Toast.makeText(this, "Subject: " + subject +
                            "\nStart Time: " + combinedCal.getTimeInMillis() +
                            "\nDuration: " + durationEditText.getText().toString(),
                    Toast.LENGTH_SHORT).show();

            Quiz quiz = new Quiz(
                    subject,
                    combinedCal.getTimeInMillis(),
                    Integer.parseInt(durationEditText.getText().toString()),
                    Float.parseFloat(marksPerQuestionEditText.getText().toString()),
                    Float.parseFloat(negativeMarkingEditText.getText().toString()),
                    questionsList, // Ensure this list is not null
                    resultViewSpinner.getSelectedItem().toString()
            );

            String quizId = databaseReference.push().getKey();
            // Optionally set the quizId before saving if needed
            quiz.setQuizId(quizId);

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


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (optionsCardView.getVisibility() == View.VISIBLE) {
                Rect outRect = new Rect();
                optionsCardView.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    optionsCardView.setVisibility(View.GONE);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (semesterSpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a semester", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (subjectSpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a subject", Toast.LENGTH_SHORT).show();
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
