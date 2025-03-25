package com.example.abcd.AuthenTication.regestration;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.cloudinary.android.MediaManager;
import com.example.abcd.AuthenTication.login.loginActivity;
import com.example.abcd.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPassword;
    private TextInputEditText editTextName, editTextEmail, editTextPassword;
    private MaterialButton buttonNext;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private String imageString;
    private ImageView ivProfilePic;
    private View[] strengthIndicators;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);


        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dqlhjvblv");
        config.put("api_key", "965822312279393");
        config.put("api_secret", "OhXXmqN1MluEb5uX0gPYbNPnfd0");
        config.put("secure", "true");
        try {
            MediaManager.init(this, config);
        } catch (IllegalStateException e) {
            // MediaManager already initialized, ignore
            Log.d("RegistrationActivity", "MediaManager already initialized");
        }
        TextView tvSignIn = findViewById(R.id.tvSignIn);
        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        editTextName = findViewById(R.id.etName);
        editTextEmail = findViewById(R.id.etEmail);
        editTextPassword = findViewById(R.id.etPassword);
        buttonNext = findViewById(R.id.btnNext);
        strengthIndicators = new View[]{
                findViewById(R.id.strengthIndicator1),
                findViewById(R.id.strengthIndicator2),
                findViewById(R.id.strengthIndicator3),
                findViewById(R.id.strengthIndicator4)
        };editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrengthIndicator(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
        updatePasswordStrengthIndicator(editTextPassword.getText().toString());
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());

        AutoCompleteTextView autoCompleteUserRole = findViewById(R.id.autoCompleteRole);
        String[] roles = {"Teacher", "Student", "Admin"};
        ArrayAdapter<String> adapterUserRole = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, roles);
        autoCompleteUserRole.setAdapter(adapterUserRole);
        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, loginActivity.class);
                startActivity(intent);
            }
        });
        buttonNext.setOnClickListener(view -> {
            tilName.setError(null);
            tilEmail.setError(null);
            tilPassword.setError(null);

            String nameInput = editTextName.getText().toString().trim();
            String emailInput = editTextEmail.getText().toString().trim();
            String passwordInput = editTextPassword.getText().toString().trim();
            String roleInput = autoCompleteUserRole.getText().toString();

            boolean isValid = true;

            if (nameInput.isEmpty()) {
                tilName.setError("Name is required");
                isValid = false;
            }

            if (emailInput.isEmpty()) {
                tilEmail.setError("Email is required");
                isValid = false;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                tilEmail.setError("Please enter a valid email");
                isValid = false;
            }

            if (passwordInput.isEmpty()) {
                tilPassword.setError("Password is required");
                isValid = false;
            } else if (passwordInput.length() < 6) {
                tilPassword.setError("Password must be at least 6 characters");
                isValid = false;
            }

            if (imageString == null || imageString.isEmpty()) {
                Toast.makeText(RegistrationActivity.this, "Please select a profile image", Toast.LENGTH_SHORT).show();
                isValid = false;
            }

            if (!isValid) {
                Toast.makeText(RegistrationActivity.this, "Please correct the errors", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(RegistrationActivity.this, Registration2.class);
            intent.putExtra("name", nameInput);
            intent.putExtra("email", emailInput);
            intent.putExtra("password", passwordInput);
            intent.putExtra("role", roleInput);
            intent.putExtra("imageString", imageString);
            startActivity(intent);
        });
    }
    public void openImageChooser(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivProfilePic.setImageURI(imageUri);
            uploadImageToCloudinary(imageUri);
        }
    }
    private void updatePasswordStrengthIndicator(String password) {
        int strength = calculateStrength(password);
        setIndicatorColor(strength);
    }private int calculateStrength(String password) {
        int strength = 0;

        // Criterion 1: Length >= 8 characters
        if (password.length() >= 8) strength++;

        // Criterion 2: Contains at least one uppercase letter
        if (password.matches(".*[A-Z].*")) strength++;

        // Criterion 3: Contains at least one number
        if (password.matches(".*\\d.*")) strength++;

        // Criterion 4: Contains at least one special character
        if (password.matches(".*[!@#$%^&*].*")) strength++;

        return strength; // Returns 0 to 4
    }private void setIndicatorColor(int strength) {
        int[] colors = {
                ContextCompat.getColor(this, R.color.strength_red),
                ContextCompat.getColor(this, R.color.strength_light_red),
                ContextCompat.getColor(this, R.color.strength_yellow),
                ContextCompat.getColor(this, R.color.strength_green)
        };
        int gray = ContextCompat.getColor(this, R.color.strength_gray);

        for (int i = 0; i < 4; i++) {
            if (i < strength) {
                strengthIndicators[i].setBackgroundColor(colors[i]);
            } else {
                strengthIndicators[i].setBackgroundColor(gray);
            }
        }
    }
    private void uploadImageToCloudinary(Uri imageUri) {
        CircularProgressIndicator progressIndicator = findViewById(R.id.progressIndicator);
        progressIndicator.setVisibility(View.VISIBLE);

        MediaManager.get()
                .upload(imageUri)
                .option("resource_type", "image")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        imageString = (String) resultData.get("secure_url");
                        progressIndicator.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressIndicator.setVisibility(View.GONE);
                        Toast.makeText(RegistrationActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                })
                .dispatch();
    }
}
