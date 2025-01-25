package com.example.abcd.userSearch;

public class UserStats {
    private int earnedPoints;
    private int totalPoints;
    private int totalQuizzes;

    public UserStats() {
        // Required for Firebase
    }

    public UserStats(int earnedPoints, int totalPoints, int totalQuizzes) {
        this.earnedPoints = earnedPoints;
        this.totalPoints = totalPoints;
        this.totalQuizzes = totalQuizzes;
    }

    // Getters and Setters
    public int getEarnedPoints() { return earnedPoints; }
    public int getTotalPoints() { return totalPoints; }
    public int getTotalQuizzes() { return totalQuizzes; }
    // Add setters if needed
}
