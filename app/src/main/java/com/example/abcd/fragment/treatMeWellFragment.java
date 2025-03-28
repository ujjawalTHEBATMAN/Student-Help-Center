package com.example.abcd.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.ApiButtonAdapter;
import com.example.abcd.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class treatMeWellFragment extends Fragment {

    private RecyclerView recyclerView;
    private ApiButtonAdapter adapter;
    private List<Map<String, Object>> apiList;
    private DatabaseReference apiKeysRef;

    public treatMeWellFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_treat_me_well, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewUsers); // Assuming recyclerViewUsers is in your treatMeWellFragment.xml
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3)); // Display in a grid of 3 columns

        apiList = new ArrayList<>();
        adapter = new ApiButtonAdapter(getContext(), apiList);
        recyclerView.setAdapter(adapter);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        apiKeysRef = database.getReference("api_keys");

        fetchApiKeys();

        return view;
    }

    private void fetchApiKeys() {
        apiKeysRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                apiList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> apiConfig = (Map<String, Object>) snapshot.getValue();
                    if (apiConfig != null) {
                        apiList.add(apiConfig);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to fetch API keys: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}