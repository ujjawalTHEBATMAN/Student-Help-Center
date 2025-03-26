package com.example.abcd.AuthenTication.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.abcd.AuthenTication.regestration.RegistrationActivity;
import com.example.abcd.R;
import com.example.abcd.databinding.ActivityLoginBinding;
import com.example.abcd.mainDashBoard;
import com.example.abcd.utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class loginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Login Page");

        Log.d("LoginActivity", "onCreate called");
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(loginActivity.this, mainDashBoard.class));
            finish();
            return;
        }

        // Login button click listener
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("LoginActivity", "Login button clicked");
                final String emailInput = binding.etUser.getText().toString().trim();
                final String passwordInput = binding.etPassword.getText().toString().trim();
                if (TextUtils.isEmpty(emailInput) || TextUtils.isEmpty(passwordInput)) {
                    Toast.makeText(loginActivity.this, "Please enter your email and password", Toast.LENGTH_SHORT).show();
                    return;
                }
                String sanitizedEmail = emailInput.replace(".", ",");
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                usersRef.child(sanitizedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String storedPassword = snapshot.child("password").getValue(String.class);
                            if (passwordInput.equals(storedPassword)) {
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

        // Sign Up text click listener
        binding.tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(loginActivity.this, RegistrationActivity.class));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}