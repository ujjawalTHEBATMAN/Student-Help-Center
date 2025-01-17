package com.example.abcd;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class gemini extends AppCompatActivity {

    private EditText userInput;
    private TextView chatDisplay;
    private Button sendButton;
    private final OkHttpClient client = new OkHttpClient();
    private final String apiKey = "dc7dd6d249msh4ab1b81cc537a3ep1c5b9cjsn1eb8f6503c00"; // Replace with your actual API key
    private final String apiHost = "copilot5.p.rapidapi.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gemini);

        // Initialize UI components
        userInput = findViewById(R.id.user_input_gemini);
        chatDisplay = findViewById(R.id.chat_display_gemini);
        sendButton = findViewById(R.id.send_button_gemini);

        // Set up LinearLayout
        LinearLayout layout = findViewById(R.id.main_layout);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String prompt = userInput.getText().toString().trim();
                if (!prompt.isEmpty()) {
                    chatDisplay.append("\nYou: " + prompt);
                    sendRequestToAPI(prompt);
                    userInput.setText(""); // Clear input field
                } else {
                    Toast.makeText(gemini.this, "Please enter a message!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendRequestToAPI(String prompt) {
        String url = "https://copilot5.p.rapidapi.com/copilot";
        MediaType mediaType = MediaType.parse("application/json");

        JSONObject payload = new JSONObject();
        try {
            payload.put("message", prompt);
            payload.put("conversation_id", null); // Replaced JSONObject.NULL with null
            payload.put("tone", "BALANCED");
            payload.put("markdown", false);
            payload.put("photo_url", null); // Replaced JSONObject.NULL with null
        } catch (JSONException e) {
            Log.e("JSON_ERROR", "Error creating JSON payload: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(mediaType, payload.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("x-rapidapi-key", apiKey)
                .addHeader("x-rapidapi-host", apiHost)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("API_ERROR", "Request failed: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(gemini.this, "Failed to connect to API", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String apiResponse = response.body().string();
                    Log.d("API_RESPONSE", apiResponse); // Log raw response
                    try {
                        JSONObject jsonObject = new JSONObject(apiResponse);
                        JSONObject dataObject = jsonObject.getJSONObject("data");
                        String aiResponse = dataObject.optString("message", "No response available");

                        // Append AI response to chatDisplay on UI thread
                        runOnUiThread(() -> {
                            try {
                                chatDisplay.append("\nAI: " + aiResponse);
                                Log.d("UI_UPDATE", "Appended AI response: " + aiResponse);
                            } catch (Exception e) {
                                Log.e("UI_ERROR", "Failed to update chatDisplay: " + e.getMessage());
                            }
                        });
                    } catch (JSONException e) {
                        Log.e("JSON_ERROR", "Failed to parse JSON: " + e.getMessage());
                        runOnUiThread(() -> Toast.makeText(gemini.this, "Error parsing API response", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e("API_ERROR", "Error in response: " + response.message());
                    runOnUiThread(() -> Toast.makeText(gemini.this, "API error occurred", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}