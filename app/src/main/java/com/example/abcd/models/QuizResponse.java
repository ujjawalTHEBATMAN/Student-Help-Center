package com.example.abcd.models;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class QuizResponse {
    @SerializedName("questions")
    private List<Question> questions;

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public static class Question {
        @SerializedName("id")
        private int id;

        @SerializedName("question")
        private String question;

        @SerializedName("options")
        private List<String> options;

        @SerializedName("correctAnswer")
        private int correctAnswer;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }

        public int getCorrectAnswer() {
            return correctAnswer;
        }

        public void setCorrectAnswer(int correctAnswer) {
            this.correctAnswer = correctAnswer;
        }
    }
}
