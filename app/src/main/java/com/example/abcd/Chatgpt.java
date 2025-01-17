package com.example.abcd;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Chatgpt extends AppCompatActivity {

    private EditText userInput;
    private TextView chatDisplay;
    private Button sendButton;
    private final OkHttpClient client = new OkHttpClient();
    private final String apiKey = "dc7dd6d249msh4ab1b81cc537a3ep1c5b9cjsn1eb8f6503c00";
    private final String apiHost = "free-chatgpt-api.p.rapidapi.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatgpt);

        userInput = findViewById(R.id.user_input);
        chatDisplay = findViewById(R.id.chat_display);
        sendButton = findViewById(R.id.send_button);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String prompt = userInput.getText().toString().trim();
                if (!prompt.isEmpty()) {
                    chatDisplay.append("\nYou: " + prompt);
                    sendRequestToAPI(prompt);
                    userInput.setText(""); // Clear input field
                } else {
                    Toast.makeText(Chatgpt.this, "Please enter a message!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendRequestToAPI(String prompt) {
        String url = "https://free-chatgpt-api.p.rapidapi.com/chat-completion-one?prompt=" + prompt;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-key", apiKey)
                .addHeader("x-rapidapi-host", apiHost)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("API_ERROR", "Request failed: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(Chatgpt.this, "Failed to connect to API", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String apiResponse = response.body().string();
                    try {
                        // Parse JSON to extract the "response" field
                        org.json.JSONObject jsonObject = new org.json.JSONObject(apiResponse);
                        String aiResponse = jsonObject.optString("response", "No response available");

                        // Display the AI response in the chat
                        runOnUiThread(() -> chatDisplay.append("\nAI: " + aiResponse));
                    } catch (org.json.JSONException e) {
                        Log.e("JSON_ERROR", "Failed to parse JSON: " + e.getMessage());
                        runOnUiThread(() -> Toast.makeText(Chatgpt.this, "Error parsing API response", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e("API_ERROR", "Error in response: " + response.message());
                    runOnUiThread(() -> Toast.makeText(Chatgpt.this, "API error occurred", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

}
