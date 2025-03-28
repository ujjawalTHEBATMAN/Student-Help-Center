package com.example.abcd;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DynamicModelsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ApiButtonAdapter adapter;
    private List<Map<String, Object>> apiList;
    private DatabaseReference apiKeysRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_models);

        recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        apiList = new ArrayList<>();
        adapter = new ApiButtonAdapter(this, apiList);
        recyclerView.setAdapter(adapter);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        apiKeysRef = database.getReference("api_keys");

        fetchApiKeys();
    }

    private void fetchApiKeys() {
        apiKeysRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DynamicModelsActivity.this, "Failed to fetch API keys: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}