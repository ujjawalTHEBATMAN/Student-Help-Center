package com.example.abcd.ExamQuizes;

import java.util.ArrayList;
import java.util.List;

public class QuizAttempt {
    private String attemptId;
    private String email;
    private String subject;
    private List<QuestionAttempt> questions;

    public QuizAttempt() {
        questions = new ArrayList<>();
    }

    // Getters and setters
    public String getAttemptId() { return attemptId; }
    public void setAttemptId(String attemptId) { this.attemptId = attemptId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public List<QuestionAttempt> getQuestions() { return questions; }
    public void setQuestions(List<QuestionAttempt> questions) { this.questions = questions; }
}

class QuestionAttempt {
    private String question;
    private String correctAnswer;
    private String userAnswer;
    private Options options;

    // Getters and setters
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }
    public Options getOptions() { return options; }
    public void setOptions(Options options) { this.options = options; }
}

class Options {
    private String option1;
    private String option2;
    private String option3;
    private String option4;

    // Getters and setters
    public String getOption1() { return option1; }
    public void setOption1(String option1) { this.option1 = option1; }
    public String getOption2() { return option2; }
    public void setOption2(String option2) { this.option2 = option2; }
    public String getOption3() { return option3; }
    public void setOption3(String option3) { this.option3 = option3; }
    public String getOption4() { return option4; }
    public void setOption4(String option4) { this.option4 = option4; }
}