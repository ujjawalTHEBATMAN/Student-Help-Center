package com.example.abcd;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.abcd.firebaseLogin.HelperClassPOJO;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditUserActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etUserRole;
    private MaterialButton btnSave;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.edit_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Retrieve email passed from ProfileFragment
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null) {
            Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show();
            finish();
        }

        etUsername = findViewById(R.id.etUsername);
        etUserRole = findViewById(R.id.etUserRole);
        btnSave = findViewById(R.id.btnSave);

        // (Optional) Pre-load the current user data from Firebase here to pre-fill the fields
        // For simplicity, we assume the user already has data loaded into these fields.
        // You might add a Firebase listener to load the current username and userRole.

        btnSave.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();
            String newUserRole = etUserRole.getText().toString().trim();

            if (TextUtils.isEmpty(newUsername) || TextUtils.isEmpty(newUserRole)) {
                Toast.makeText(EditUserActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            updateUserProfile(newUsername, newUserRole);
        });
    }

    private void updateUserProfile(String newUsername, String newUserRole) {
        // Sanitize email for Firebase key usage (replace '.' with ',')
        String sanitizedEmail = userEmail.replace(".", ",");
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(sanitizedEmail);

        // Create a map with updated fields
        // (If there are additional fields to update, add them here)
        userRef.child("user").setValue(newUsername, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@NonNull DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    // Update the user role as well
                    userRef.child("userRole").setValue(newUserRole, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@NonNull DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error == null) {
                                Toast.makeText(EditUserActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(EditUserActivity.this, "Error updating user role: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(EditUserActivity.this, "Error updating username: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
