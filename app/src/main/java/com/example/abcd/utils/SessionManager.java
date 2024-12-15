package com.example.abcd.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "LoginSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_IMAGE = "profile_image";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // Set login state with email
    public void setLogin(boolean isLoggedIn, String email) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Save profile image resource ID
    public void saveProfileImage(int imageResourceId) {
        editor.putInt(KEY_PROFILE_IMAGE, imageResourceId);
        editor.apply();
    }

    // Get saved profile image resource ID
    public int getProfileImage() {
        return pref.getInt(KEY_PROFILE_IMAGE, -1);
    }

    // Get user's email
    public String getEmail() {
        return pref.getString(KEY_EMAIL, null);
    }

    // Logout and clear all saved data
    public void logout() {
        editor.clear();
        editor.apply();
    }
}