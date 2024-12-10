package com.example.abcd;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView randomQuote;
    private ImageView centerImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        randomQuote = findViewById(R.id.randomQuote);
        centerImage = findViewById(R.id.centerImage);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        centerImage.startAnimation(fadeIn);
        randomQuote.startAnimation(fadeIn);



        fetchRandomQuote();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this,loginActivity2.class));
            }
        }, 4000);


    }

    //quote  retriever method
    private void fetchRandomQuote() {
        String url = "https://api.api-ninjas.com/v1/quotes?category=happiness";
        String apiKey = "JkqJr45guAaQ25QOSZ/v1Q==vdnXnQTmYdXxW6ww";

        // volley all program
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {



            // response
            @Override
                    public void onResponse(JSONArray response) {
                        try {
                            if (response.length() > 0) {
                                JSONObject quoteObject = response.getJSONObject(0);
                                String quote = quoteObject.getString("quote");
                                randomQuote.setText(quote);
                            } else {
                                randomQuote.setText("no quote ");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            randomQuote.setText("parse fail");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                randomQuote.setText("sorry quote access failed");
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Api-Key", apiKey);
                return headers;
            }
        };

        queue.add(jsonArrayRequest);
    }
}
