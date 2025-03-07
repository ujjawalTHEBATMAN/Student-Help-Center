package com.example.abcd.AuthenTication.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.abcd.AuthenTication.regestration.RegistrationActivity;
import com.example.abcd.R;
import com.example.abcd.mainDashBoard;
import com.example.abcd.utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class loginActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private SessionManager sessionManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize the new SessionManager
        sessionManager = new SessionManager(this);

        // Redirect if already logged in
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(loginActivity.this, mainDashBoard.class));
            finish();
            return;
        }

        // Initialize UI components (consider renaming etUser to etEmail in layout)
        editTextEmail = findViewById(R.id.etUser);
        editTextPassword = findViewById(R.id.etPassword);
        buttonLogin = findViewById(R.id.btnLogin);
        textViewRegister = findViewById(R.id.tvRegister);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String emailInput = editTextEmail.getText().toString().trim();
                final String passwordInput = editTextPassword.getText().toString().trim();

                if (TextUtils.isEmpty(emailInput) || TextUtils.isEmpty(passwordInput)) {
                    Toast.makeText(loginActivity.this, "Please enter your email and password", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Sanitize email key (replace '.' with ',')
                String sanitizedEmail = emailInput.replace(".", ",");

                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                usersRef.child(sanitizedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String storedPassword = snapshot.child("password").getValue(String.class);
                            if (passwordInput.equals(storedPassword)) {
                                // Successful login: update session using new SessionManager
                                sessionManager.setLogin(true, emailInput);
                                startActivity(new Intent(loginActivity.this, mainDashBoard.class));
                                finish();
                            } else {
                                Toast.makeText(loginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(loginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(loginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(loginActivity.this, RegistrationActivity.class));
            }
        });
    }
}
