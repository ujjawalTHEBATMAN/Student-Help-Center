package com.example.abcd;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagingActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private Button buttonSend;
    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();
    private Map<String, Object> apiConfig;
    private Gson gson = new Gson();
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        // Initialize RecyclerView and Adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewMessages.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(messageList);
        recyclerViewMessages.setAdapter(messageAdapter);

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Get API configuration from Intent
        String apiJson = getIntent().getStringExtra("api_data");
        if (apiJson != null) {
            apiConfig = gson.fromJson(apiJson, Map.class);
            Log.d("MessagingActivity", "Received API Config: " + apiConfig);
        } else {
            Log.e("MessagingActivity", "No API configuration received!");
            finish(); // Close the activity if no API data is available
            return;
        }

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userMessage = editTextMessage.getText().toString().trim();
                if (!userMessage.isEmpty()) {
                    addMessage("user", userMessage);
                    editTextMessage.getText().clear();
                    sendToAI(userMessage);
                }
            }
        });
    }

    private void addMessage(String sender, String content) {
        messageList.add(new Message(content, sender));
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerViewMessages.scrollToPosition(messageList.size() - 1); // Scroll to the latest message
    }

    private void sendToAI(String userMessage) {
        if (apiConfig == null) {
            Log.e("MessagingActivity", "Error: apiConfig is null. No API configuration available.");
            addMessage("ai", "Error: No API configuration available.");
            return;
        }

        Log.d("MessagingActivity", "Sending message to AI with config: " + apiConfig.toString());

        String baseUrl = (String) apiConfig.get("base_url");
        String endpoint = (String) apiConfig.get("endpoint");
        String method = (String) apiConfig.get("method");
        String headersStr = (String) apiConfig.get("headers");
        String requestBodyStr = (String) apiConfig.get("request_body");

        Map<String, Object> headersMap = gson.fromJson(headersStr, Map.class);
        Map<String, Object> requestBodyMap = gson.fromJson(requestBodyStr, Map.class);

        String url = baseUrl + endpoint;
        Log.d("MessagingActivity", "Constructed API URL: " + url);

        JSONObject requestBodyJson = new JSONObject();
        if (requestBodyMap != null) {
            try {
                for (Map.Entry<String, Object> entry : requestBodyMap.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        if (((String) value).contains("{user_message}")) {
                            requestBodyJson.put(key, ((String) value).replace("{user_message}", userMessage));
                        } else {
                            requestBodyJson.put(key, value);
                        }
                    } else if (value instanceof List) {
                        JSONArray jsonArray = new JSONArray();
                        for (Object item : (List) value) {
                            if (item instanceof Map) {
                                JSONObject innerJson = new JSONObject((Map) item);
                                if (innerJson.has("content") && innerJson.getString("content").contains("{user_message}")) {
                                    innerJson.put("content", innerJson.getString("content").replace("{user_message}", userMessage));
                                }
                                jsonArray.put(innerJson);
                            } else {
                                jsonArray.put(item);
                            }
                        }
                        requestBodyJson.put(key, jsonArray);
                    } else {
                        requestBodyJson.put(key, value);
                    }
                }
                Log.d("MessagingActivity", "Constructed Request Body JSON: " + requestBodyJson.toString(2));
            } catch (JSONException e) {
                Log.e("MessagingActivity", "Error creating request body JSON: " + e.getMessage());
                addMessage("ai", "Error processing your request.");
                return;
            }
        }

        int volleyMethod = (method != null && method.equalsIgnoreCase("POST")) ? Request.Method.POST : Request.Method.GET;

        StringRequest stringRequest = new StringRequest(volleyMethod, url,
                response -> {
                    try {
                        Log.d("MessagingActivity", "AI Response Received: " + response);
                        // Parse the response as a JSONArray
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject resultObject = jsonArray.getJSONObject(0); // Get the first object
                        String aiResponse = resultObject.getString("generated_text");
                        addMessage("ai", aiResponse);
                    } catch (JSONException e) {
                        Log.e("MessagingActivity", "Error parsing AI response: " + e.getMessage());
                        addMessage("ai", "Error processing AI response.");
                    }
                }, error -> {
            Log.e("MessagingActivity", "AI API Error: " + error.toString());
            if (error.networkResponse != null) {
                Log.e("MessagingActivity", "AI API Error Response Code: " + error.networkResponse.statusCode);
                Log.e("MessagingActivity", "AI API Error Response Data: " + new String(error.networkResponse.data));
            }
            addMessage("ai", "Error communicating with the AI.");
        }) {
            @Override
            public byte[] getBody() {
                return requestBodyJson.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                if (headersMap != null) {
                    for (Map.Entry<String, Object> entry : headersMap.entrySet()) {
                        if (entry.getValue() instanceof String) {
                            headers.put(entry.getKey(), (String) entry.getValue());
                        }
                    }
                    Log.d("MessagingActivity", "Constructed Headers for Request: " + headers.toString());
                }
                return headers;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

}