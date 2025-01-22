package com.example.abcd.MathFeature;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.example.abcd.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EquationSolver extends AppCompatActivity {

    // UI Components
    private Spinner spinnerOperation;
    private TextInputEditText etEquation;
    private ProgressBar progressBar;
    private TextView tvError, tvOperationResult, tvExpressionResult, tvFinalResult;
    private MaterialCardView resultContainer, statusCard;
    private MaterialButton btnSolve, btnReset;

    // API Service
    private NewtonApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equation_solver);

        // Initialize Views
        spinnerOperation = findViewById(R.id.spinnerOperation);
        etEquation = findViewById(R.id.etEquation);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        btnSolve = findViewById(R.id.btnSolve);
        btnReset = findViewById(R.id.btnReset);
        resultContainer = findViewById(R.id.resultContainer);
        statusCard = findViewById(R.id.statusCard);
        tvOperationResult = findViewById(R.id.tvOperationResult);
        tvExpressionResult = findViewById(R.id.tvExpressionResult);
        tvFinalResult = findViewById(R.id.tvFinalResult);
        MaterialButton btnShare = findViewById(R.id.btnShare);
        btnShare.setOnClickListener(v -> {
            if (resultContainer.getVisibility() == View.VISIBLE) {
                shareResult();
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                        "Solve an equation first!", Snackbar.LENGTH_SHORT).show();
            }
        });



        // Setup Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.equation_operations,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOperation.setAdapter(adapter);

        // Retrofit Setup
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://newton.vercel.app/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(NewtonApiService.class);

        // Button Click Listeners
        btnSolve.setOnClickListener(v -> solveEquation());
        btnReset.setOnClickListener(v -> resetForm());
    }

    private void solveEquation() {
        String operation = spinnerOperation.getSelectedItem().toString().toLowerCase();
        String expression = etEquation.getText().toString().trim();

        if (expression.isEmpty()) {
            showError("Please enter an equation");
            return;
        }

        String formattedExpression = expression.replace(" ", "_");
        showLoading(true);

        apiService.solveEquation(operation, formattedExpression).enqueue(new Callback<NewtonResponse>() {
            @Override
            public void onResponse(Call<NewtonResponse> call, Response<NewtonResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    displayResult(response.body());
                } else {
                    showError("Failed to solve equation");
                }
            }

            @Override
            public void onFailure(Call<NewtonResponse> call, Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }
    // Add this method to the class
    private void shareResult() {
        try {
            String operation = tvOperationResult.getText().toString().replace("Operation: ", "");
            String expression = tvExpressionResult.getText().toString().replace("Expression: ", "");
            String result = tvFinalResult.getText().toString();

            String shareText = String.format(
                    "Math Solver Result\n\n" +
                            "Operation: %s\n" +
                            "Equation: %s\n" +
                            "Solution: %s\n\n" +
                            "Solved using %s",
                    operation, expression, result, getString(R.string.app_name)
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Equation Solution");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            startActivity(Intent.createChooser(shareIntent, "Share Solution"));

        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Error sharing result", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void displayResult(NewtonResponse result) {
        resultContainer.setVisibility(View.VISIBLE);
        statusCard.setVisibility(View.GONE);

        tvOperationResult.setText(String.format("Operation: %s", result.getOperation()));
        tvExpressionResult.setText(String.format("Expression: %s", result.getExpression()));
        tvFinalResult.setText(result.getResult());
    }

    private void resetForm() {
        etEquation.setText("");
        resultContainer.setVisibility(View.GONE);
        statusCard.setVisibility(View.GONE);
        spinnerOperation.setSelection(0);
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        statusCard.setVisibility(loading ? View.VISIBLE : View.GONE);
        tvError.setVisibility(View.GONE);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        statusCard.setVisibility(View.VISIBLE);
    }
}