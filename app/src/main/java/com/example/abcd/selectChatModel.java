package com.example.abcd;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class selectChatModel extends AppCompatActivity {

    private List<Model> models;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_chat_model);

        //chatgpt button click on listener
        Button bchatgpt=findViewById(R.id.button_chatgpt);
        bchatgpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(selectChatModel.this,Chatgpt.class));
            }
        });

        // gemini button click on listener
        Button bchatgpt1=findViewById(R.id.button_gemini);
        bchatgpt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(selectChatModel.this,gemini.class));
            }
        });

        Button bchatgpt2=findViewById(R.id.button_midjourney);
        bchatgpt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(selectChatModel.this,midjourney.class));
            }
        });

        // Initialize models
        models = new ArrayList<>();
        models.add(new Model("Mistral-7B", "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.3"));
        models.add(new Model("Phi-3-mini", "https://api-inference.huggingface.co/models/microsoft/Phi-3-mini-4k-instruct"));
        models.add(new Model("gemma-2-2b", "https://api-inference.huggingface.co/models/google/gemma-2-2b-it"));
        models.add(new Model("distilgpt2", "https://api-inference.huggingface.co/models/distilgpt2"));
        models.add(new Model("blenderbot_small-90M", "https://api-inference.huggingface.co/models/facebook/blenderbot_small-90M"));
        models.add(new Model("DialoGPT-small", "https://api-inference.huggingface.co/models/microsoft/DialoGPT-small"));
        models.add(new Model("Gpt2Medium","https://api-inference.huggingface.co/models/openai-community/gpt2-medium"));
        // Find buttons and set click listeners
        MaterialCardView buttonMistral = findViewById(R.id.button_mistral);
        MaterialCardView  buttonPhi = findViewById(R.id.button_phi);
        MaterialCardView  buttonGemma = findViewById(R.id.button_gemma);
        MaterialCardView  buttonDistilGPT2 = findViewById(R.id.button_distilgpt2);
        MaterialCardView  buttonBlenderBot = findViewById(R.id.button_blenderbot);
        MaterialCardView  buttonDialoGPT = findViewById(R.id.button_dialogpt);
        MaterialCardView buttonGpt2Medium=findViewById(R.id.button_gpt2Midium);

        // Set onClick listeners for each button
        setOnClickListener(buttonMistral, 0);
        setOnClickListener(buttonPhi, 1);
        setOnClickListener(buttonGemma, 2);
        setOnClickListener(buttonDistilGPT2, 3);
        setOnClickListener(buttonBlenderBot, 4);
        setOnClickListener(buttonDialoGPT, 5);
        setOnClickListener(buttonGpt2Medium,6);
    }

    private void setOnClickListener(final MaterialCardView  button, final int index) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Model selectedModel = models.get(index);
                // Start chatting activity and pass the API URL
                Intent intent = new Intent(selectChatModel.this, chataiwithdistilgpt2.class);
                intent.putExtra("API_URL", selectedModel.getApiUrl());
                startActivity(intent);
            }
        });
    }
}