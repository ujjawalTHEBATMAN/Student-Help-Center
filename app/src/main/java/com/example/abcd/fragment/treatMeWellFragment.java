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
    private static final String GITHUB_SHC_URL = "https://github.com/ujjawalTHEBATMAN/Student-Help-Center";
    private static final String GITHUB_DEV_URL = "https://github.com/ujjawalTHEBATMAN/";

    public treatMeWellFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_treat_me_well, container, false);


        setupButton(view, R.id.btnGithubSHC, GITHUB_SHC_URL);


        setupDeveloperButtons(view);

        return view;
    }

    private void setupDeveloperButtons(View view) {
        // GitHub buttons
        for (int i = 1; i <= 5; i++) {
            int githubId = getResources().getIdentifier("btnGithub" + i, "id", requireContext().getPackageName());
            int linkedinId = getResources().getIdentifier("btnLinkedin" + i, "id", requireContext().getPackageName());

            setupButton(view, githubId, GITHUB_DEV_URL);
            setupButton(view, linkedinId, GITHUB_DEV_URL);

            // Setup LeetCode button only for the first team member
            if (i == 1) {
                int leetcodeId = getResources().getIdentifier("btnLeetcode1", "id", requireContext().getPackageName());
                setupButton(view, leetcodeId, GITHUB_DEV_URL);
            }
        }
    }

    private void setupButton(View view, int buttonId, final String url) {
        MaterialButton button = view.findViewById(buttonId);
        if (button != null) {
            button.setOnClickListener(v -> openUrl(url));
        }
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            // Verify that there's an app that can handle this intent
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Consider adding error handling or user feedback here
        }
    }
}