package com.example.abcd.MathFeature;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.abcd.R;

public class EquationSolutionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equation_solution);

        TextView tvOperation = findViewById(R.id.tvOperation);
        TextView tvExpression = findViewById(R.id.tvExpression);
        TextView tvResult = findViewById(R.id.tvResult);
        TextView tvSteps = findViewById(R.id.tvSteps);

        String operation = getIntent().getStringExtra("operation");
        String expression = getIntent().getStringExtra("expression");
        String result = getIntent().getStringExtra("result");

        tvOperation.setText(String.format("Operation: %s", operation));
        tvExpression.setText(String.format("Expression: %s", expression));
        tvResult.setText(String.format("Result: %s", result));
    }
}