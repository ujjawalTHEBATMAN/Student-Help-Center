package com.example.abcd.userSearch;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.abcd.R;
import com.example.abcd.fragment.ProfileFragment;

public class UserProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        String userEmail = getIntent().getStringExtra("USER_EMAIL");

        ProfileFragment profileFragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("USER_EMAIL", userEmail);
        profileFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .commit();
    }
}