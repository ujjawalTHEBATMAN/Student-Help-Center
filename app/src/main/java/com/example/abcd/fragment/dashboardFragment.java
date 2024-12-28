package com.example.abcd.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;

import com.example.abcd.QuizActivity;
import com.example.abcd.R;
import com.example.abcd.SemestersActivity;
import com.google.android.material.button.MaterialButton;

public class dashboardFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard2, container, false);

        // Find buttons in the Study Material section
        MaterialButton btnNotes = view.findViewById(R.id.btnNotes);
        MaterialButton btnAssignments = view.findViewById(R.id.btnAssignments);
        MaterialButton btnResources = view.findViewById(R.id.btnResources);

        // Set click listeners for each button
        btnNotes.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.abcd.videoplayers1.MainActivity.class);
            startActivity(intent);
        });

        btnAssignments.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(),  QuizActivity.class);
            startActivity(intent);
        });

        btnResources.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SemestersActivity.class);
            startActivity(intent);
        });

        return view;
    }
}
