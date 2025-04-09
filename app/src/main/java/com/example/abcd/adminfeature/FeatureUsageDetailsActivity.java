package com.example.abcd.adminfeature;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.abcd.R;
import com.example.abcd.view.CustomGraphView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
    private TextView tvMostUsedFeature;
    private TextView tvTotalUsage;
    private TextView tvDate;
    private MaterialButton btnRefresh;
    private LinearLayout featureLegendContainer;

    // Feature count TextViews
    private TextView tvIscCount;
    private TextView tvDocsCount;
    private String userEmail;

    // Map of feature keys to their display names
    private final Map<String, String> featureKeyToDisplay = new HashMap<String, String>() {{
        put("imagesize_compressor", "ISC");
        put("doc_scanner", "DOCS");
        put("math_feature", "MATH");
        put("message", "MSG");
        put("cgpa_calculator", "CGPC");
        put("video", "VID");
        put("quizzes", "QUIZ");
        put("old_paper", "PAPS");
    }};

    // Map of feature display names to their TextView IDs
    private final Map<String, Integer> featureDisplayToViewId = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_usage_details);

        // Retrieve the user email passed via intent; if absent, terminate the activity.
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null) {
            finish();
            return;
        }

        initializeViews();
        setupToolbar();
        setupFeatureViewIds();

        // Display the user email in the appropriate TextView.
        tvUserEmail.setText(userEmail);

        // Set current date
        tvDate.setText(new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date()));

        // Set refresh button click listener
        btnRefresh.setOnClickListener(v -> {
            loadFeatureUsageData(userEmail);
            Toast.makeText(this, "Refreshing data...", Toast.LENGTH_SHORT).show();
        });

        // Load feature usage data from Firebase for the specified user.
        loadFeatureUsageData(userEmail);
    }

    /**
     * Setup the mapping between feature display names and their TextView IDs
     */
    private void setupFeatureViewIds() {
        featureDisplayToViewId.put("ISC", R.id.tvIscCount);
        featureDisplayToViewId.put("DOCS", R.id.tvDocsCount);
        // Add other feature TextView IDs here as needed
    }

    /**
     * Binds UI components to their corresponding views.
     */
    private void initializeViews() {
        graphView = findViewById(R.id.featureUsageGraph);
        progressBar = findViewById(R.id.progressBar);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvNoDataMessage = findViewById(R.id.tvNoDataMessage);
        tvMostUsedFeature = findViewById(R.id.tvMostUsedFeature);
        tvTotalUsage = findViewById(R.id.tvTotalUsage);
        tvDate = findViewById(R.id.tvDate);
        btnRefresh = findViewById(R.id.btnRefresh);
        featureLegendContainer = findViewById(R.id.featureLegendContainer);

        // Initialize feature count TextViews
        tvIscCount = findViewById(R.id.tvIscCount);
        tvDocsCount = findViewById(R.id.tvDocsCount);
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
        graphView.setVisibility(View.GONE);

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
                            int totalUsage = 0;
                            String mostUsedFeature = "";
                            int maxUsage = 0;

                            // Retrieve feature usage data using four-digit abbreviations.
                            for (Map.Entry<String, String> entry : featureKeyToDisplay.entrySet()) {
                                String featureKey = entry.getKey();
                                String displayName = entry.getValue();

                                Long value = featureSnapshot.child(featureKey).getValue(Long.class);
                                if (value != null && value > 0) {
                                    int usageCount = value.intValue();
                                    featureData.put(displayName, usageCount);
                                    totalUsage += usageCount;

                                    // Update most used feature
                                    if (usageCount > maxUsage) {
                                        maxUsage = usageCount;
                                        mostUsedFeature = displayName;
                                    }

                                    // Update feature count in details section
                                    updateFeatureCountView(displayName, usageCount);
                                } else {
                                    // Set feature count to 0 if no data
                                    updateFeatureCountView(displayName, 0);
                                }
                            }

                            if (!featureData.isEmpty()) {
                                // Update the summary section
                                tvMostUsedFeature.setText(mostUsedFeature);
                                tvTotalUsage.setText(String.valueOf(totalUsage));

                                // Update the graph view with the retrieved data.
                                graphView.setData(featureData);
                                graphView.setVisibility(View.VISIBLE);
                                tvNoDataMessage.setVisibility(View.GONE);
                            } else {
                                displayNoDataMessage();
                                resetSummarySection();
                            }
                        } else {
                            displayNoDataMessage();
                            resetSummarySection();
                            resetFeatureCountViews();
                        }
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        displayNoDataMessage();
                        resetSummarySection();
                        resetFeatureCountViews();
                        Toast.makeText(FeatureUsageDetailsActivity.this,
                                "Failed to load data: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Updates the TextView showing the count for a specific feature.
     *
     * @param displayName The display name of the feature.
     * @param count The usage count for the feature.
     */
    private void updateFeatureCountView(String displayName, int count) {
        Integer viewId = featureDisplayToViewId.get(displayName);
        if (viewId != null) {
            TextView countView = findViewById(viewId);
            if (countView != null) {
                countView.setText(String.valueOf(count));
            }
        }
    }

    /**
     * Resets all feature count views to zero.
     */
    private void resetFeatureCountViews() {
        for (Integer viewId : featureDisplayToViewId.values()) {
            TextView countView = findViewById(viewId);
            if (countView != null) {
                countView.setText("0");
            }
        }
    }

    /**
     * Resets the summary section when no data is available.
     */
    private void resetSummarySection() {
        tvMostUsedFeature.setText("-");
        tvTotalUsage.setText("0");
    }

    /**
     * Displays a message indicating that no data is available and hides the graph view.
     */
    private void displayNoDataMessage() {
        graphView.setVisibility(View.GONE);
        tvNoDataMessage.setVisibility(View.VISIBLE);
    }
}