package com.example.abcd.models;

public class UserStats {
    private int totalQuizzes;
    private int totalPoints;
    private int earnedPoints;

    public UserStats() {
        // Default constructor required for Firebase
    }

    public UserStats(int totalQuizzes, int totalPoints, int earnedPoints) {
        this.totalQuizzes = totalQuizzes;
        this.totalPoints = totalPoints;
        this.earnedPoints = earnedPoints;
    }

    // Getters and Setters
    public int getTotalQuizzes() {
        return totalQuizzes;
    }

    public void setTotalQuizzes(int totalQuizzes) {
        this.totalQuizzes = totalQuizzes;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getEarnedPoints() {
        return earnedPoints;
    }

    public void setEarnedPoints(int earnedPoints) {
        this.earnedPoints = earnedPoints;
    }

    // Helper methods to update stats
    public void incrementTotalQuizzes() {
        this.totalQuizzes++;
    }

    public void addPoints(int points) {
        this.earnedPoints += points;
        this.totalPoints += points;
    }
}
