package com.example.abcd.AuthenTication.regestration;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.abcd.R;
import com.example.abcd.mainDashBoard;
import com.example.abcd.utils.SessionManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class Registration3 extends AppCompatActivity {

    private Button btnRegister;
    private String regName, regEmail, regPassword, regRole, images;
    private DatabaseReference usersRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration3);

        btnRegister = findViewById(R.id.btnRegister);

        // Receive the registration data from previous activities.
        Intent intent = getIntent();
        regName = intent.getStringExtra("name");  // Used as the display name
        regEmail = intent.getStringExtra("email");
        regPassword = intent.getStringExtra("password");
        regRole = intent.getStringExtra("role");
        images = intent.getStringExtra("imagess");

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check that regName is not empty.
                if (regName == null || regName.isEmpty()) {
                    Toast.makeText(Registration3.this, "Username is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Prepare the user data according to your JSON format.
                Map<String, Object> userData = new HashMap<>();
                userData.put("email", regEmail);
                userData.put("imageSend", images);
                userData.put("password", regPassword);

                Map<String, Object> stats = new HashMap<>();
                stats.put("earnedPoints", 0);
                stats.put("totalPoints", 0);
                stats.put("totalQuizzes", 0);
                userData.put("stats", stats);

                userData.put("user", regName);
                userData.put("userRole", regRole);

                // Use sanitized email (replace '.' with ',') as the Firebase key.
                String sanitizedEmail = regEmail.replace(".", ",");
                usersRef.child(sanitizedEmail).setValue(userData)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Use the new SessionManager.
                                SessionManager sessionManager = new SessionManager(Registration3.this);
                                sessionManager.setLogin(true, regEmail);
                                Intent dashboardIntent = new Intent(Registration3.this, mainDashBoard.class);
                                startActivity(dashboardIntent);
                                finish();
                            } else {
                                Toast.makeText(Registration3.this, "Registration failed: "
                                        + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
