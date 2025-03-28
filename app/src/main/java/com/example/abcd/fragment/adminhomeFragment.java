package com.example.abcd.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.abcd.R;
import com.example.abcd.userSearch.userSearchingActivity;

import com.example.abcd.adminfeature.insertAiApiKey;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link adminhomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class adminhomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private Button adminActionButton1; // Declare the first button
    private Button adminActionButton2; // Declare the second button

    public adminhomeFragment() {
        // Required empty public constructor
    }

    public static adminhomeFragment newInstance(String param1, String param2) {
        adminhomeFragment fragment = new adminhomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adminhome, container, false);

        // Get the reference to the first button
        adminActionButton1 = view.findViewById(R.id.adminActionButton1);

        // Set OnClickListener for the first button
        adminActionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), userSearchingActivity.class);
                startActivity(intent);
            }
        });

        // Get the reference to the second button
        adminActionButton2 = view.findViewById(R.id.adminActionButton2);

        // Set OnClickListener for the second button
        adminActionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to insertAiApiKey
                Intent intent = new Intent(getActivity(), insertAiApiKey.class);
                startActivity(intent);
            }
        });

        return view;
    }
}