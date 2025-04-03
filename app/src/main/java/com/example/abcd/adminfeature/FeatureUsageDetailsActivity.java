package com.example.abcd.adminfeature;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.abcd.R;
import com.example.abcd.view.CustomGraphView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Activity to display detailed usage statistics for application features.
 * <p>
 * This class initializes the user interface components, sets up the toolbar with proper navigation,
 * and loads the feature usage data from Firebase. If the data is available, it is displayed on a custom
 * graph view; otherwise, a no-data message is shown.
 * </p>
 */
public class FeatureUsageDetailsActivity extends AppCompatActivity {

    private CustomGraphView graphView;
    private ProgressBar progressBar;
    private TextView tvUserEmail;
    private TextView tvNoDataMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_usage_details);

        // Retrieve the user email passed via intent; if absent, terminate the activity.
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null) {
            finish();
            return;
        }

        initializeViews();
        setupToolbar();

        // Display the user email in the appropriate TextView.
        tvUserEmail.setText(userEmail);

        // Load feature usage data from Firebase for the specified user.
        loadFeatureUsageData(userEmail);
    }

    /**
     * Binds UI components to their corresponding views.
     */
    private void initializeViews() {
        graphView = findViewById(R.id.featureUsageGraph);
        progressBar = findViewById(R.id.progressBar);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvNoDataMessage = findViewById(R.id.tvNoDataMessage);
    }

    /**
     * Sets up the toolbar with a formal title and a back navigation listener.
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            // Set a formal title for the toolbar.
            getSupportActionBar().setTitle("Feature Usage Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    /**
     * Loads the feature usage data for a given user from Firebase.
     *
     * @param userEmail The email of the user whose data is to be loaded.
     */
    private void loadFeatureUsageData(String userEmail) {
        // Format the current date for the Firebase path.
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        // Replace dots in the email to generate a valid Firebase key.
        String userKey = userEmail.replace(".", ",");

        progressBar.setVisibility(View.VISIBLE);
        tvNoDataMessage.setVisibility(View.GONE);

        FirebaseDatabase.getInstance()
                .getReference("analytics")
                .child(todayDate)
                .child("users")
                .child(userKey)
                .child("feature_touch")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Process the snapshot if data is available.
                        if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                            DataSnapshot featureSnapshot = snapshot.getChildren().iterator().next();
                            Map<String, Integer> featureData = new LinkedHashMap<>();

                            // Retrieve feature usage data using four-digit abbreviations.
                            addFeatureIfExists(featureData, featureSnapshot, "imagesize_compressor", "ISC");
                            addFeatureIfExists(featureData, featureSnapshot, "doc_scanner", "DOCS");
                            addFeatureIfExists(featureData, featureSnapshot, "math_feature", "MATH");
                            addFeatureIfExists(featureData, featureSnapshot, "message", "MSG");
                            addFeatureIfExists(featureData, featureSnapshot, "cgpa_calculator", "CGPC");
                            addFeatureIfExists(featureData, featureSnapshot, "video", "VID");
                            addFeatureIfExists(featureData, featureSnapshot, "quizzes", "QUIZ");
                            addFeatureIfExists(featureData, featureSnapshot, "old_paper", "PAPS");

                            if (!featureData.isEmpty()) {
                                // Update the graph view with the retrieved data.
                                graphView.setData(featureData);
                                graphView.setVisibility(View.VISIBLE);
                                tvNoDataMessage.setVisibility(View.GONE);
                            } else {
                                displayNoDataMessage();
                            }
                        } else {
                            displayNoDataMessage();
                        }
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        displayNoDataMessage();
                    }
                });
    }

    /**
     * Adds a feature's usage count to the data map if it exists and has a value greater than zero.
     *
     * @param featureData Map to hold feature display names and their corresponding usage counts.
     * @param snapshot    DataSnapshot containing the feature data.
     * @param featureKey  The key representing the feature in the Firebase data.
     * @param displayName The abbreviated display name for the feature.
     */
    private void addFeatureIfExists(Map<String, Integer> featureData, DataSnapshot snapshot,
                                    String featureKey, String displayName) {
        Long value = snapshot.child(featureKey).getValue(Long.class);
        if (value != null && value > 0) {
            featureData.put(displayName, value.intValue());
        }
    }

    /**
     * Displays a message indicating that no data is available and hides the graph view.
     */
    private void displayNoDataMessage() {
        graphView.setVisibility(View.GONE);
        tvNoDataMessage.setVisibility(View.VISIBLE);
    }
}
