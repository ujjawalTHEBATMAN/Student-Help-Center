package com.example.abcd.ExamQuizes;

import java.util.Map;

public class Question {
    private String text;
    private Map<String, String> options;
    private int correctAnswerIndex;

    public Question() {}

    public Question(String text, Map<String, String> options, int correctAnswerIndex) {
        this.text = text;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
    }

    // Getters and setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Map<String, String> getOptions() { return options; }
    public void setOptions(Map<String, String> options) { this.options = options; }

    public int getCorrectAnswerIndex() { return correctAnswerIndex; }
    public void setCorrectAnswerIndex(int correctAnswerIndex) { this.correctAnswerIndex = correctAnswerIndex; }
}
