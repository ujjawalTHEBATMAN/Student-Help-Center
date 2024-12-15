package com.example.abcd.firebaseLogin;

public class HelperClassPOJO {
    private String email;
    private String password;
    private String user;
    private String userRole;
    private int postsCount;
    private int followersCount;
    private int followingCount;
    private String profileImageUrl;
    private long registrationTimestamp;

    // Default constructor (required for Firebase)
    public HelperClassPOJO() {
        // Initialize default values
        this.postsCount = 0;
        this.followersCount = 0;
        this.followingCount = 0;
        this.userRole = "User";
        this.registrationTimestamp = System.currentTimeMillis();
    }

    // Constructor with basic registration details
    public HelperClassPOJO(String email, String password, String user) {
        this.email = email;
        this.password = password;
        this.user = user;
        this.postsCount = 0;
        this.followersCount = 0;
        this.followingCount = 0;
        this.userRole = "User";
        this.registrationTimestamp = System.currentTimeMillis();
    }

    // Comprehensive constructor
    public HelperClassPOJO(String email, String password, String user,
                           String userRole, String profileImageUrl) {
        this.email = email;
        this.password = password;
        this.user = user;
        this.userRole = userRole;
        this.profileImageUrl = profileImageUrl;
        this.postsCount = 0;
        this.followersCount = 0;
        this.followingCount = 0;
        this.registrationTimestamp = System.currentTimeMillis();
    }

    // Getters and Setters
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public int getPostsCount() {
        return postsCount;
    }

    public void setPostsCount(int postsCount) {
        this.postsCount = postsCount;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public long getRegistrationTimestamp() {
        return registrationTimestamp;
    }

    public void setRegistrationTimestamp(long registrationTimestamp) {
        this.registrationTimestamp = registrationTimestamp;
    }
}