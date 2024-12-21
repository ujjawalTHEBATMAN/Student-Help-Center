package com.example.abcd.models;

import java.util.ArrayList;
import java.util.List;

public class Message {
    private String messageText;
    private String userName;
    private long timestamp;
    private String formattedTime;
    private List<String> likedBy;
    private int totalLikes;
    private String profilePicUrl;
    private int likes;
    private String imageURL;

    public Message() {
        // Default constructor required for Firebase
        this.likedBy = new ArrayList<>();
        this.totalLikes = 0;
        this.timestamp = System.currentTimeMillis();
        updateFormattedTime();
    }

    public Message(String messageText, String userName) {
        this.messageText = messageText;
        this.userName = userName;
        this.likedBy = new ArrayList<>();
        this.totalLikes = 0;
        this.timestamp = System.currentTimeMillis();
        updateFormattedTime();
    }

    private void updateFormattedTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm, dd MMM");
        this.formattedTime = sdf.format(new java.util.Date(timestamp));
    }

    // Getters and Setters
    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        updateFormattedTime();
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    public List<String> getLikedBy() {
        if (likedBy == null) {
            likedBy = new ArrayList<>();
        }
        return likedBy;
    }

    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
        this.totalLikes = likedBy != null ? likedBy.size() : 0;
    }

    public int getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(int totalLikes) {
        this.totalLikes = totalLikes;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    // Like functionality
    public boolean toggleLike(String userId) {
        if (likedBy == null) {
            likedBy = new ArrayList<>();
        }
        
        if (likedBy.contains(userId)) {
            likedBy.remove(userId);
            totalLikes = Math.max(0, totalLikes - 1);
            return false; // Unliked
        } else {
            likedBy.add(userId);
            totalLikes++;
            return true; // Liked
        }
    }

    public boolean isLikedByUser(String userId) {
        return likedBy != null && likedBy.contains(userId);
    }
}
