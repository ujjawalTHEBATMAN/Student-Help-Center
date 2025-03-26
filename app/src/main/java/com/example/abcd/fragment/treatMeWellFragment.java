package com.example.abcd.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.abcd.Chatgpt;
import com.example.abcd.Model;
import com.example.abcd.chataiwithdistilgpt2;
import com.example.abcd.gemini;
import com.example.abcd.midjourney;
import com.example.abcd.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class treatMeWellFragment extends Fragment {

    private List<Model> models;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_treat_me_well, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // MaterialCardView Click Listeners for top cards
        MaterialCardView cardChatGPT = view.findViewById(R.id.card_chatgpt);
        cardChatGPT.setOnClickListener(v -> startActivity(new Intent(requireActivity(), Chatgpt.class)));

        MaterialCardView cardGemini = view.findViewById(R.id.card_gemini);
        cardGemini.setOnClickListener(v -> startActivity(new Intent(requireActivity(), gemini.class)));

        MaterialCardView cardMidjourney = view.findViewById(R.id.card_midjourney);
        cardMidjourney.setOnClickListener(v -> startActivity(new Intent(requireActivity(), midjourney.class)));

        // Initialize Models
        models = new ArrayList<>();
        models.add(new Model("Mistral-7B", "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.3"));
        models.add(new Model("Phi-3-mini", "https://api-inference.huggingface.co/models/microsoft/Phi-3-mini-4k-instruct"));
        models.add(new Model("gemma-2-2b", "https://api-inference.huggingface.co/models/google/gemma-2-2b-it"));
        models.add(new Model("distilgpt2", "https://api-inference.huggingface.co/models/distilgpt2"));
        models.add(new Model("blenderbot_small-90M", "https://api-inference.huggingface.co/models/facebook/blenderbot_small-90M"));
        models.add(new Model("DialoGPT-small", "https://api-inference.huggingface.co/models/microsoft/DialoGPT-small"));
        models.add(new Model("Gpt2Medium", "https://api-inference.huggingface.co/models/openai-community/gpt2-medium"));

        // Setup MaterialCardView Click Listeners for grid cards
        setupCardClick(view, R.id.button_mistral, 0);
        setupCardClick(view, R.id.button_phi, 1);
        setupCardClick(view, R.id.button_gemma, 2);
        setupCardClick(view, R.id.button_distilgpt2, 3);
        setupCardClick(view, R.id.button_blenderbot, 4);
        setupCardClick(view, R.id.button_dialogpt, 5);
        setupCardClick(view, R.id.button_gpt2Midium, 6);
    }

    private void setupCardClick(View parentView, int cardId, int modelIndex) {
        MaterialCardView card = parentView.findViewById(cardId);
        card.setOnClickListener(v -> {
            Model selectedModel = models.get(modelIndex);
            Intent intent = new Intent(requireActivity(), chataiwithdistilgpt2.class);
            intent.putExtra("API_URL", selectedModel.getApiUrl());
            startActivity(intent);
        });
    }
}