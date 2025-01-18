package com.example.abcd.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.abcd.R;
import com.google.android.material.button.MaterialButton;

public class treatMeWellFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_treat_me_well, container, false);

        // Project GitHub button
        MaterialButton btnGithubSHC = view.findViewById(R.id.btnGithubSHC);

        // Team member 1 buttons
        MaterialButton btnGithub1 = view.findViewById(R.id.btnGithub1);
        MaterialButton btnLinkedin1 = view.findViewById(R.id.btnLinkedin1);
        MaterialButton btnLeetcode1 = view.findViewById(R.id.btnLeetcode1);

        // Team member 2 buttons
        MaterialButton btnGithub2 = view.findViewById(R.id.btnGithub2);
        MaterialButton btnLinkedin2 = view.findViewById(R.id.btnLinkedin2);

        // Team member 3 buttons
        MaterialButton btnGithub3 = view.findViewById(R.id.btnGithub3);
        MaterialButton btnLinkedin3 = view.findViewById(R.id.btnLinkedin3);

        // Team member 4 buttons
        MaterialButton btnGithub4 = view.findViewById(R.id.btnGithub4);
        MaterialButton btnLinkedin4 = view.findViewById(R.id.btnLinkedin4);

        // Team member 5 buttons
        MaterialButton btnGithub5 = view.findViewById(R.id.btnGithub5);
        MaterialButton btnLinkedin5 = view.findViewById(R.id.btnLinkedin5);

        // Project GitHub link
        btnGithubSHC.setOnClickListener(v -> openUrl("https://github.com/ujjawalTHEBATMAN/Student-Help-Center"));


        btnGithub1.setOnClickListener(v -> openUrl("https://github.com/ujjawalTHEBATMAN"));
        btnLinkedin1.setOnClickListener(v -> openUrl("https://www.linkedin.com/in/ujjawal-vishwakarma-aba5b6303/"));
        btnLeetcode1.setOnClickListener(v -> openUrl("https://leetcode.com/u/ujjawalvishwakarma266/"));


        btnGithub2.setOnClickListener(v -> openUrl("https://github.com/madara"));
        btnLinkedin2.setOnClickListener(v -> openUrl("https://linkedin.com/in/madara"));


        btnGithub3.setOnClickListener(v -> openUrl("https://github.com/itachi"));
        btnLinkedin3.setOnClickListener(v -> openUrl("https://linkedin.com/in/itachi"));


        btnGithub4.setOnClickListener(v -> openUrl("https://github.com/kakashi"));
        btnLinkedin4.setOnClickListener(v -> openUrl("https://linkedin.com/in/kakashi"));


        btnGithub5.setOnClickListener(v -> openUrl("https://github.com/primagan"));
        btnLinkedin5.setOnClickListener(v -> openUrl("https://linkedin.com/in/primagan"));

        return view;
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            // You might want to show a toast or snackbar here to inform the user if the link cannot be opened
        }
    }
}