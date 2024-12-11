package com.example.abcd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class loginActivity1 extends AppCompatActivity {

    EditText eml, pswd;
    Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login1);

        eml = findViewById(R.id.editTextEmail);
        pswd = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.loginButton);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validEmail() && validPassword()) {
                    checkUser();
                }
            }
        });
    }

    // Validating Email
    public Boolean validEmail() {
        String val = eml.getText().toString();

        if (val.isEmpty()) {
            eml.setError("Email cannot be empty");
            return false;
        } else if (!val.contains("@") || !val.contains(".")) {
            eml.setError("Invalid email format");
            return false;
        }
        return true;
    }

    // Validating Password
    public Boolean validPassword() {
        String val = pswd.getText().toString();

        if (val.isEmpty()) {
            pswd.setError("Password cannot be empty");
            return false;
        }
        return true;
    }

    // Checking User Login Credentials
    public void checkUser() {
        String email = eml.getText().toString().trim();
        String password = pswd.getText().toString().trim();

        // Replace '.' with ',' in email to avoid Firebase path issues
        String sanitizedEmail = email.replace(".", ",");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");

        // Query to check if email exists in the database
        Query checkEmail = ref.child(sanitizedEmail);  // Direct reference to the sanitized email as the key

        checkEmail.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String dbPassword = snapshot.child("password").getValue(String.class);

                    if (dbPassword != null && dbPassword.equals(password)) {
                        Toast.makeText(loginActivity1.this, "Login Successful", Toast.LENGTH_SHORT).show();

                        // You can navigate to another activity here, e.g., MainActivity
                    startActivity(new Intent(loginActivity1.this,mainDashBoard.class));
                    } else {
                        pswd.setError("Incorrect password");
                    }
                } else {
                    eml.setError("No such user exists");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(loginActivity1.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
