package com.example.abcd.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.abcd.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class treatMeWellFragment extends Fragment {

    // Social media profile URLs for team members
    private static final String[] GITHUB_URLS = {
            "https://github.com/ujjawalTHEBATMAN",
            "https://github.com/ujjawalTHEBATMAN",
            "https://github.com/ujjawalTHEBATMAN",
            "https://github.com/ujjawalTHEBATMAN",
            "https://github.com/ujjawalTHEBATMAN"
    };

    private static final String[] LINKEDIN_URLS = {
            "https://www.linkedin.com/in/ujjawal-vishwakarma-aba5b6303/",
            "https://www.linkedin.com/in/ujjawal-vishwakarma-aba5b6303/",
            "https://www.linkedin.com/in/ujjawal-vishwakarma-aba5b6303/",
            "https://www.linkedin.com/in/ujjawal-vishwakarma-aba5b6303/",
            "https://www.linkedin.com/in/ujjawal-vishwakarma-aba5b6303/"
    };

    private static final String[] LEETCODE_URLS = {
            "https://leetcode.com/u/ujjawalvishwakarma266/",
            null,
            null,
            null,
            null
    };

    // View references
    private MaterialButton btnGithubProject;
    private MaterialButton[] githubButtons = new MaterialButton[5];
    private MaterialButton[] linkedinButtons = new MaterialButton[5];
    private MaterialButton[] leetcodeButtons = new MaterialButton[5];
    private MaterialCardView[] teamMemberCards = new MaterialCardView[5];

    public treatMeWellFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_treat_me_well, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupClickListeners();
        setupAnimations();
    }

    private void initializeViews(View view) {
        btnGithubProject = view.findViewById(R.id.btnGithubSHC);

        // Initialize GitHub buttons
        for (int i = 0; i < 5; i++) {
            int githubId = getResources().getIdentifier("btnGithub" + (i + 1), "id", requireContext().getPackageName());
            githubButtons[i] = view.findViewById(githubId);
        }

        // Initialize LinkedIn buttons
        for (int i = 0; i < 5; i++) {
            int linkedinId = getResources().getIdentifier("btnLinkedin" + (i + 1), "id", requireContext().getPackageName());
            linkedinButtons[i] = view.findViewById(linkedinId);
        }

        // Initialize LeetCode button (only for first team member)
        leetcodeButtons[0] = view.findViewById(R.id.btnLeetcode1);

        // Initialize team member cards
        for (int i = 0; i < 5; i++) {
            int cardId = getResources().getIdentifier("card_team_member_" + (i + 1), "id", requireContext().getPackageName());
            teamMemberCards[i] = view.findViewById(cardId);
        }
    }

    private void setupClickListeners() {
        // Project GitHub button
        btnGithubProject.setOnClickListener(v ->
                openUrl("https://github.com/ujjawalTHEBATMAN/Student-Help-Center")
        );

        // Set up GitHub button listeners
        for (int i = 0; i < githubButtons.length; i++) {
            final int index = i;
            if (githubButtons[i] != null) {
                githubButtons[i].setOnClickListener(v -> openUrl(GITHUB_URLS[index]));
            }
        }

        // Set up LinkedIn button listeners
        for (int i = 0; i < linkedinButtons.length; i++) {
            final int index = i;
            if (linkedinButtons[i] != null) {
                linkedinButtons[i].setOnClickListener(v -> openUrl(LINKEDIN_URLS[index]));
            }
        }

        // Set up LeetCode button listener (only for first team member)
        if (leetcodeButtons[0] != null) {
            leetcodeButtons[0].setOnClickListener(v -> openUrl(LEETCODE_URLS[0]));
        }
    }

    private void setupAnimations() {
        // Add elevation animation on card touch
        for (MaterialCardView card : teamMemberCards) {
            if (card != null) {
                card.setOnTouchListener((v, event) -> {
                    switch (event.getAction()) {
                        case android.view.MotionEvent.ACTION_DOWN:
                            // Increase elevation on touch
                            card.setCardElevation(16f);
                            return true;
                        case android.view.MotionEvent.ACTION_UP:
                        case android.view.MotionEvent.ACTION_CANCEL:
                            // Restore original elevation
                            card.setCardElevation(8f);
                            return true;
                    }
                    return false;
                });
            }
        }
    }

    private void openUrl(String url) {
        if (url == null || url.isEmpty()) {
            showToast("Profile not available");
            return;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            showToast("Unable to open URL: " + e.getMessage());
        }
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up references
        btnGithubProject = null;
        githubButtons = null;
        linkedinButtons = null;
        leetcodeButtons = null;
        teamMemberCards = null;
    }
}