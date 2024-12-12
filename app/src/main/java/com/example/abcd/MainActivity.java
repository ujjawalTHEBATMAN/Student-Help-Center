package com.example.abcd;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.abcd.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // Check if the user is already logged in
        if (sessionManager.isLoggedIn()) {
            // If logged in, directly go to the Main Dashboard
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(MainActivity.this, mainDashBoard.class));
                    finish();
                }
            }, 3000); // 3 seconds splash screen delay
        } else {
            // If not logged in, go to the Registration screen
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(MainActivity.this, loginActivity2.class));
                    finish();
                }
            }, 3000); // 3 seconds splash screen delay
        }
    }
}
