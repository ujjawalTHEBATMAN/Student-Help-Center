package com.example.abcd;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.abcd.firebaseLogin.HelperClassPOJO;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class loginActivity2 extends AppCompatActivity {

    EditText email, password, username;
    Button button, buttonLoginRedirection;
    FirebaseDatabase firebase;
    DatabaseReference reference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        // UI Components
        email = findViewById(R.id.editTextTextEmailAddress2);
        password = findViewById(R.id.editTextTextPassword);
        button = findViewById(R.id.button);
        username = findViewById(R.id.editTextusername);
        buttonLoginRedirection = findViewById(R.id.loginRedirectionButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailInput = email.getText().toString().trim();
                String passwordInput = password.getText().toString().trim();
                String usernameInput = username.getText().toString().trim();

                // Validate Inputs
                if (!isValidInput(emailInput, passwordInput, usernameInput)) return;

                // Firebase instance
                firebase = FirebaseDatabase.getInstance();
                reference = firebase.getReference("users");

                // Sanitize email input to use as a valid Firebase Database key
                String sanitizedEmail = emailInput.replace(".", ",");

                // Check if email already exists
                reference.child(sanitizedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            email.setError("Email is already registered");
                            Toast.makeText(loginActivity2.this, "Email already exists!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Save user data
                            HelperClassPOJO helperClass = new HelperClassPOJO(emailInput, passwordInput, usernameInput);
                            reference.child(sanitizedEmail).setValue(helperClass);

                            Toast.makeText(loginActivity2.this, "You as " + usernameInput + " are registered successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(loginActivity2.this, loginActivity1.class);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(loginActivity2.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Redirect to Login Activity
        buttonLoginRedirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(loginActivity2.this, loginActivity1.class));
            }
        });
    }

    // Helper Method: Input Validation
    private boolean isValidInput(String emailInput, String passwordInput, String usernameInput) {
        if (emailInput.isEmpty()) {
            email.setError("Email cannot be empty");
            return false;
        } else if (!emailInput.contains("@") || !emailInput.contains(".")) {
            email.setError("Enter a valid email");
            return false;
        }
        if (passwordInput.isEmpty()) {
            password.setError("Password cannot be empty");
            return false;
        } else if (passwordInput.length() < 6) {
            password.setError("Password must be at least 6 characters long");
            return false;
        }
        if (usernameInput.isEmpty()) {
            username.setError("Username cannot be empty");
            return false;
        }
        return true;
    }
}
