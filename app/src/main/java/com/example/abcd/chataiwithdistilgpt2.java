package com.example.abcd;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.chataihelperclass.ApiHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class chataiwithdistilgpt2 extends AppCompatActivity {

    private EditText editTextMessage;
    private Button buttonSend;
    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private ApiHelper apiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chataiwithdistilgpt2);

        // Retrieve API URL from intent
        String apiUrl = getIntent().getStringExtra("API_URL");
        apiHelper = new ApiHelper(apiUrl);

        // Initialize views
        buttonSend = findViewById(R.id.buttonSend);
        editTextMessage = findViewById(R.id.editTextMessage);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);

        // Initialize RecyclerView
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messageAdapter);

        // Handle send button click
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    // Add user message to RecyclerView
                    messageList.add(new Message(message, "user"));
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerViewMessages.scrollToPosition(messageList.size() - 1);

                    // Clear the input field
                    editTextMessage.setText("");

                    // Send message to API
                    new SendMessageTask().execute(message);
                }
            }
        });
    }

    private class SendMessageTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... messages) {
            return apiHelper.getResponseFromAPI(messages[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            // Filter and add AI response to RecyclerView
            if (result != null && !result.isEmpty()) {
                try {
                    // Parse JSON to extract "generated_text"
                    String extractedResponse = extractGeneratedText(result);

                    // Filter the extracted response
                    String filteredResponse = filterResponse(extractedResponse);

                    // Add the filtered response to the RecyclerView
                    messageList.add(new Message(filteredResponse, "ai"));
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                } catch (Exception e) {
                    Log.e("Response Parsing", "Failed to parse or filter the response", e);
                    messageList.add(new Message("Error processing AI response.", "ai"));
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                }
            } else {
                Log.e("API Response", "Received empty response from API!");
            }
        }
    }


    private String filterResponse(String response) {
        if (response == null || response.isEmpty()) {
            return "No response received from AI.";
        }

        // Step 1: Remove escape sequences and redundant whitespace
        response = response.replaceAll("\\\\[ntr]", "").trim();

        // Step 2: Remove unnecessary repetitions (e.g., repeated sentences or phrases)
        String[] sentences = response.split("\\. ");
        StringBuilder cleanedResponse = new StringBuilder();

        // Use a LinkedHashSet to ensure unique sentences while maintaining their order
        Set<String> uniqueSentences = new LinkedHashSet<>();
        for (String sentence : sentences) {
            sentence = sentence.trim(); // Trim each sentence
            if (!uniqueSentences.contains(sentence) && sentence.length() > 5) { // Only add non-duplicate and meaningful sentences
                uniqueSentences.add(sentence);
            }
        }

        // Step 3: Rebuild the cleaned response
        for (String sentence : uniqueSentences) {
            cleanedResponse.append(sentence).append(". ");
        }

        // Return the final cleaned text
        return cleanedResponse.toString().trim();
    }


    private String extractGeneratedText(String rawResponse) {
        try {

            JSONArray jsonArray = new JSONArray(rawResponse);
            if (jsonArray.length() > 0) {
                JSONObject firstObject = jsonArray.getJSONObject(0);
                return firstObject.optString("generated_text", "No text found");
            }
        } catch (JSONException e) {
            Log.e("JSON Parsing", "Failed to parse JSON response", e);
        }
        return "no text";
    }
}
