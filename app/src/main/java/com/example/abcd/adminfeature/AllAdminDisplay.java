package com.example.abcd.adminfeature;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.abcd.R;
import com.example.abcd.utils.SessionManager;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class AllAdminDisplay extends AppCompatActivity {

    private LogStorageHelper logStorageHelper;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_admin_display);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        logStorageHelper = new LogStorageHelper();

        // Get current user email from session
        SessionManager sessionManager = new SessionManager(this);
        currentUserEmail = sessionManager.getEmail();

        Button crashButton = findViewById(R.id.btnCrash);
        crashButton.setOnClickListener(v -> {
            logStorageHelper.log("Crashlytics", "Admin triggered a test crash", currentUserEmail);  // Using current user email
            FirebaseCrashlytics.getInstance().recordException(
                    new RuntimeException("Test Crash from Crashlytics button!")
            );
            throw new RuntimeException("Test Crash from Crashlytics button!");
        });

        Button deepAnalyzeButton = findViewById(R.id.btnDeepAnalyze);
        deepAnalyzeButton.setOnClickListener(v -> {
            logStorageHelper.log("Navigation", "Opening Deep Analysis");
            Intent intent = new Intent(AllAdminDisplay.this, CrashlyticsDeepAnalysis.class);
            startActivity(intent);
        });
    }
}
