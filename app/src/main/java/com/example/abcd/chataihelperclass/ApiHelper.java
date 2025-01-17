package com.example.abcd.chataihelperclass;

import android.util.Log;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;
import com.google.gson.Gson;

public class ApiHelper {
    private String apiUrl;
    private static final String BEARER_TOKEN = ""; // enter your hf model here ok try and then use it

    public ApiHelper(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getResponseFromAPI(String inputText) {
        OkHttpClient client = new OkHttpClient();

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("inputs", inputText);

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("Authorization", "Bearer " + BEARER_TOKEN)
                .addHeader("Content-Type", "application/json")
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseData = response.body().string();
                return responseData;
            } else {
                return "Error: " + response.message();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception: " + e.getMessage();
        }
    }
}
