package com.example.abcd.notificationSection;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.abcd.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotifictionActivity extends AppCompatActivity {
    private static final String SECRET_CODE = "Xujjawal1881bhumiX";
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifiction);

        // Setup toolbar with back button
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "User not recognized", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Sanitize email for Firebase key
        String sanitizedEmail = userEmail.replace(".", ",");
        userRef = FirebaseDatabase.getInstance().getReference("users").child(sanitizedEmail);

        TextInputEditText codeInput = findViewById(R.id.codeInput);
        MaterialButton submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> {
            String enteredCode = codeInput.getText().toString().trim();

            if (enteredCode.equals(SECRET_CODE)) {
                userRef.child("userRole").setValue("admin")
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(NotifictionActivity.this,
                                    "Successfully upgraded to Admin!", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(NotifictionActivity.this,
                                "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Invalid security code", Toast.LENGTH_SHORT).show();
            }
        });
    }
}