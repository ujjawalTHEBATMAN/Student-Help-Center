package com.example.abcd;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.Request;
import org.json.JSONObject;
import com.bumptech.glide.Glide;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class midjourney extends AppCompatActivity {

    private EditText editTextQuery;
    private Button buttonSubmit;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_midjourney);



        editTextQuery = findViewById(R.id.editTextQuery);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        imageView = findViewById(R.id.imageView);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = editTextQuery.getText().toString();
                if (query.isEmpty()) {
                    Toast.makeText(midjourney.this, "Please enter a query", Toast.LENGTH_SHORT).show();
                } else {
                    sendApiRequest(query);
                }
            }
        });
    }

    private void sendApiRequest(String query) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        try {
            JSONObject jsonBody = new JSONObject();
            JSONObject message = new JSONObject();
            message.put("function_name", "image_generator");
            message.put("type", "image_generation");
            message.put("query", query);
            message.put("output_type", "png");
            jsonBody.put("jsonBody", message);

            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonBody.toString(), mediaType);
            Request request = new Request.Builder()
                    .url("https://ai-image-generator14.p.rapidapi.com/")
                    .post(body)
                    .addHeader("x-rapidapi-key", "dc7dd6d249msh4ab1b81cc537a3ep1c5b9cjsn1eb8f6503c00") // Replace with your actual API key
                    .addHeader("x-rapidapi-host", "ai-image-generator14.p.rapidapi.com")
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(midjourney.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        final String responseData = response.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            String imageUrl = jsonObject.getJSONObject("message").getString("output_png");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Glide.with(midjourney.this)
                                            .load(imageUrl)
                                            .into(imageView);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(midjourney.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        final int responseCode = response.code();
                        final String errorResponse = response.body() != null ? response.body().string() : "No error message provided";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (responseCode == 403) {
                                    Toast.makeText(midjourney.this, "Access forbidden. Check your API key or permissions. Error: " + errorResponse, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(midjourney.this, "API call failed with code: " + responseCode + " Error: " + errorResponse, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error building request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}