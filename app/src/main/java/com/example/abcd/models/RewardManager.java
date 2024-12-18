package com.example.abcd.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RewardManager {
    private static final String[] REWARD_MESSAGES = {
        "Outstanding Achievement! 🌟",
        "Brilliant Performance! 🏆",
        "Knowledge Champion! 📚",
        "Quiz Master! 👑",
        "Exceptional Work! 🎯"
    };

    private static final String[] ACHIEVEMENT_TITLES = {
        "Speed Demon",
        "Perfect Score",
        "Quick Learner",
        "Knowledge Seeker",
        "Quiz Champion"
    };

    public static class Reward {
        private String title;
        private String message;
        private int points;
        private String timestamp;

        public Reward(String title, String message, int points, String timestamp) {
            this.title = title;
            this.message = message;
            this.points = points;
            this.timestamp = timestamp;
        }

        // Getters
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public int getPoints() { return points; }
        public String getTimestamp() { return timestamp; }
    }

    public static Reward calculateReward(int score, int totalQuestions, long timeSpent) {
        // Calculate percentage
        double percentage = (score * 100.0) / totalQuestions;
        
        // Base points calculation
        int points = (int) (percentage * 10);
        
        // Time bonus (if completed quickly)
        if (timeSpent < 15000) { // Less than 15 seconds per question
            points += 50;
        }

        // Perfect score bonus
        if (score == totalQuestions) {
            points += 100;
        }

        // Select random congratulatory message
        Random random = new Random();
        String message = REWARD_MESSAGES[random.nextInt(REWARD_MESSAGES.length)];
        String title = ACHIEVEMENT_TITLES[random.nextInt(ACHIEVEMENT_TITLES.length)];

        // Create timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());

        return new Reward(title, message, points, timestamp);
    }

    public static String getMotivationalMessage(int score, int totalQuestions) {
        double percentage = (score * 100.0) / totalQuestions;
        
        if (percentage == 100) {
            return "Perfect score! You're absolutely brilliant! 🌟";
        } else if (percentage >= 80) {
            return "Excellent work! Keep it up! 🎉";
        } else if (percentage >= 60) {
            return "Good effort! You're making progress! 💪";
        } else {
            return "Keep practicing! You'll do better next time! 📚";
        }
    }
}
