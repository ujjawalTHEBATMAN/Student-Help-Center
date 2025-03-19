package com.example.abcd.ExamQuizes;

public class QuizQuestion1 {
    private String question;
    private String userAnswer;
    private String correctAnswer;
    private boolean isCorrect;

    public QuizQuestion1() {
        // Required empty constructor for Firebase
    }

    public QuizQuestion1(String question, String userAnswer, String correctAnswer) {
        this.question = question;
        this.userAnswer = userAnswer;
        this.correctAnswer = correctAnswer;
        this.isCorrect = userAnswer != null && userAnswer.equals(correctAnswer);
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
        this.isCorrect = userAnswer != null && userAnswer.equals(correctAnswer);
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
        this.isCorrect = userAnswer != null && userAnswer.equals(correctAnswer);
    }

    public boolean isCorrect() {
        return isCorrect;
    }
}