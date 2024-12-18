package com.example.abcd.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizManager {
    private static final Map<String, List<QuizQuestion>> quizzesBySemester = new HashMap<>();

    static {
        // Initialize quiz questions for each semester
        initializeSem1Questions();
        initializeSem2Questions();
        initializeSem3Questions();
        // Add more semesters as needed
    }

    private static void initializeSem1Questions() {
        List<QuizQuestion> sem1Questions = new ArrayList<>();
        sem1Questions.add(new QuizQuestion(
            "What is the main purpose of an operating system?",
            new String[]{
                "To manage hardware resources",
                "To provide user interface",
                "To run applications",
                "All of the above"
            },
            "All of the above"
        ));
        sem1Questions.add(new QuizQuestion(
            "Which data structure uses LIFO?",
            new String[]{
                "Queue",
                "Stack",
                "Array",
                "Linked List"
            },
            "Stack"
        ));
        sem1Questions.add(new QuizQuestion(
            "What is the time complexity of binary search?",
            new String[]{
                "O(n)",
                "O(log n)",
                "O(n²)",
                "O(1)"
            },
            "O(log n)"
        ));
        quizzesBySemester.put("sem1", sem1Questions);
    }

    private static void initializeSem2Questions() {
        List<QuizQuestion> sem2Questions = new ArrayList<>();
        sem2Questions.add(new QuizQuestion(
            "What is inheritance in OOP?",
            new String[]{
                "Creating multiple objects",
                "Reusing code from existing classes",
                "Method overloading",
                "Data hiding"
            },
            "Reusing code from existing classes"
        ));
        sem2Questions.add(new QuizQuestion(
            "Which is not a type of inheritance?",
            new String[]{
                "Single",
                "Multiple",
                "Circular",
                "Hierarchical"
            },
            "Circular"
        ));
        quizzesBySemester.put("sem2", sem2Questions);
    }

    private static void initializeSem3Questions() {
        List<QuizQuestion> sem3Questions = new ArrayList<>();
        sem3Questions.add(new QuizQuestion(
            "What is normalization in DBMS?",
            new String[]{
                "Data encryption",
                "Data organization",
                "Data compression",
                "Data deletion"
            },
            "Data organization"
        ));
        sem3Questions.add(new QuizQuestion(
            "Which SQL command is used to modify data?",
            new String[]{
                "SELECT",
                "UPDATE",
                "CREATE",
                "DROP"
            },
            "UPDATE"
        ));
        quizzesBySemester.put("sem3", sem3Questions);
    }

    public static List<QuizQuestion> getQuizQuestions(String semester) {
        return quizzesBySemester.getOrDefault(semester, new ArrayList<>());
    }
}
