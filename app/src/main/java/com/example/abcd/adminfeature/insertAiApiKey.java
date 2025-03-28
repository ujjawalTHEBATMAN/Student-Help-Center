package com.example.abcd.adminfeature;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.abcd.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class insertAiApiKey extends AppCompatActivity {

    private EditText editTextId, editTextName, editTextEndpoint, editTextMethod,
            editTextHeaders, editTextRequestBody, editTextQueryParameters, editTextResponseFormat,
            editTextParameterName, editTextParameterType, editTextParameterRequired, editTextParameterDescription,
            editTextImageUrl;
    private Spinner spinnerBaseUrl;
    private Button buttonSaveApiKey;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_ai_api_key);

        // Initialize Firebase Database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("api_keys");

        // Get references to EditText fields and Spinner
        editTextId = findViewById(R.id.editTextId);
        editTextName = findViewById(R.id.editTextName);
        spinnerBaseUrl = findViewById(R.id.spinnerBaseUrl);
        editTextEndpoint = findViewById(R.id.editTextEndpoint);
        editTextMethod = findViewById(R.id.editTextMethod);
        editTextHeaders = findViewById(R.id.editTextHeaders);
        editTextRequestBody = findViewById(R.id.editTextRequestBody);
        editTextQueryParameters = findViewById(R.id.editTextQueryParameters);
        editTextResponseFormat = findViewById(R.id.editTextResponseFormat);
        editTextParameterName = findViewById(R.id.editTextParameterName);
        editTextParameterType = findViewById(R.id.editTextParameterType);
        editTextParameterRequired = findViewById(R.id.editTextParameterRequired);
        editTextParameterDescription = findViewById(R.id.editTextParameterDescription);
        editTextImageUrl = findViewById(R.id.editTextImageUrl);

        // Get reference to the Save button
        buttonSaveApiKey = findViewById(R.id.buttonSaveApiKey);

        // Populate the Base URL Spinner
        List<String> baseUrlOptions = new ArrayList<>();
        baseUrlOptions.add("https://api-inference.huggingface.co/models/");
        baseUrlOptions.add("https://api.openai.com/v1/chat/completions");
        baseUrlOptions.add("https://router.requesty.ai/v1");
        baseUrlOptions.add("https://generativelanguage.googleapis.com/v1beta/models/"); // Google Gemini
        baseUrlOptions.add("https://api.grok.ai/v1/"); // Grok AI
        baseUrlOptions.add("Other"); // Option for custom URL

        ArrayAdapter<String> baseUrlAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, baseUrlOptions);
        spinnerBaseUrl.setAdapter(baseUrlAdapter);

        // Set OnClickListener for the Save button
        buttonSaveApiKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveApiKeyToFirebase();
            }
        });
    }

    private void saveApiKeyToFirebase() {
        String id = editTextId.getText().toString().trim();
        String name = editTextName.getText().toString().trim();
        String baseUrl = spinnerBaseUrl.getSelectedItem().toString();
        String endpoint = editTextEndpoint.getText().toString().trim();
        String method = editTextMethod.getText().toString().trim();
        String headersStr = editTextHeaders.getText().toString().trim();
        String requestBodyStr = editTextRequestBody.getText().toString().trim();
        String queryParametersStr = editTextQueryParameters.getText().toString().trim();
        String responseFormat = editTextResponseFormat.getText().toString().trim();
        String parameterName = editTextParameterName.getText().toString().trim();
        String parameterType = editTextParameterType.getText().toString().trim();
        String parameterRequiredStr = editTextParameterRequired.getText().toString().trim();
        String parameterDescription = editTextParameterDescription.getText().toString().trim();
        String imageUrl = editTextImageUrl.getText().toString().trim();

        if (id.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "ID and Name are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> apiKeyData = new HashMap<>();
        apiKeyData.put("id", id);
        apiKeyData.put("name", name);
        apiKeyData.put("base_url", baseUrl);
        apiKeyData.put("endpoint", endpoint);
        apiKeyData.put("method", method);
        apiKeyData.put("response_format", responseFormat);
        apiKeyData.put("image_url", imageUrl);

        // Handle JSON strings for headers, request body, and query parameters
        try {
            if (!headersStr.isEmpty()) {
                apiKeyData.put("headers", new JSONObject(headersStr).toString());
            }
            if (!requestBodyStr.isEmpty()) {
                apiKeyData.put("request_body", new JSONObject(requestBodyStr).toString());
            }
            if (!queryParametersStr.isEmpty()) {
                apiKeyData.put("query_parameters", new JSONObject(queryParametersStr).toString());
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Invalid JSON format for headers, request body, or query parameters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Handle parameters (for a single parameter in this version)
        if (!parameterName.isEmpty()) {
            List<Map<String, String>> parameters = new ArrayList<>();
            Map<String, String> parameter = new HashMap<>();
            parameter.put("name", parameterName);
            parameter.put("type", parameterType);
            parameter.put("required", parameterRequiredStr);
            parameter.put("description", parameterDescription);
            parameters.add(parameter);
            apiKeyData.put("parameters", parameters);
        }

        // Push data to Firebase Realtime Database
        databaseReference.child(id).setValue(apiKeyData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(insertAiApiKey.this, "API Key configuration saved successfully", Toast.LENGTH_SHORT).show();
                    clearInputFields();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(insertAiApiKey.this, "Failed to save API Key configuration: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void clearInputFields() {
        editTextId.getText().clear();
        editTextName.getText().clear();
        spinnerBaseUrl.setSelection(0); // Reset spinner to the first item
        editTextEndpoint.getText().clear();
        editTextHeaders.getText().clear();
        editTextRequestBody.getText().clear();
        editTextQueryParameters.getText().clear();
        editTextResponseFormat.setText("json"); // Reset to default
        editTextMethod.setText("POST"); // Reset to default
        editTextParameterName.getText().clear();
        editTextParameterType.getText().clear();
        editTextParameterRequired.getText().clear();
        editTextParameterDescription.getText().clear();
        editTextImageUrl.getText().clear();
    }
}