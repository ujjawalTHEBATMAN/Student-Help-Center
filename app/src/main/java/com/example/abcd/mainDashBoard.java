package com.example.abcd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.abcd.firebaseLogin.HelperClassPOJO;
import com.example.abcd.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class mainDashBoard extends AppCompatActivity {

    private TextView usernameText;
    private ImageView profileImage;
    private TextView logoutText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dash_board);

        usernameText = findViewById(R.id.usernameText);
        profileImage = findViewById(R.id.profileImage);
        logoutText = findViewById(R.id.logoutText);

        // Fetch user data from Firebase (use session manager to get the email)
        SessionManager sessionManager = new SessionManager(this);
        String email = sessionManager.getEmail();

        // Fetch the user details from Firebase using sanitized email
        if (email != null) {
            String sanitizedEmail = email.replace(".", ",");  // Firebase path sanitization

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
            reference.child(sanitizedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        HelperClassPOJO user = snapshot.getValue(HelperClassPOJO.class);
                        if (user != null) {
                            usernameText.setText(user.getUser());  // Set username
                            // Optionally, load the profile image using Glide or Picasso
                            // Glide.with(mainDashBoard.this).load(user.getProfileImage()).into(profileImage);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(mainDashBoard.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Method to open the user's profile (called when clicking on the CardView)
    public void openProfile(View view) {
        // Navigate to Profile Editor Activity
        startActivity(new Intent(mainDashBoard.this,ProfileActivity.class));
    }

    // Logout functionality
    public void logout(View view) {
        // Log out from Firebase Authentication
        FirebaseAuth.getInstance().signOut();

        // Clear the session (logout from the app)
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.logout();

        // Redirect to the login screen
        Intent intent = new Intent(mainDashBoard.this, loginActivity1.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();  // Finish this activity to remove it from the back stack
    }
}
