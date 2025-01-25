package com.example.abcd.firebaseLogin;

import com.example.abcd.models.UserStats;

public class HelperClassPOJO {
    private String user;
    private String email;
    private String password;
    private String userRole;
    private UserStats stats;
    private String imageSend;
    public HelperClassPOJO() {
        // Default constructor required for Firebase
    }

    public HelperClassPOJO(String user, String email, String password, String userRole) {
        this.user = user;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.stats = new UserStats(0, 0, 0);
        this.imageSend = "";  // <--- ADD THIS LINE HERE
    }

    // Getters and Setters
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public UserStats getStats() {
        return stats;
    }

    public void setStats(UserStats stats) {
        this.stats = stats;
    }

    public String getImageSend() { return imageSend; }
    public void setImageSend(String imageSend) { this.imageSend = imageSend; }
}