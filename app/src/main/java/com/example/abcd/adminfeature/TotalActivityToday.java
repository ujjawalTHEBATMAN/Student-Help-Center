package com.example.abcd.adminfeature;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.abcd.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TotalActivityToday extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ActiveUserAdapter adapter;
    private List<ActiveUser> userList = new ArrayList<>();
    private List<ActiveUser> filteredList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView tvTotalCount, tvCurrentTime;
    private SearchView searchView;
    private FloatingActionButton fabRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_today);
        initializeViews();
        setupRecyclerView();
        loadTodayActiveUsers();

        fabRefresh.setOnClickListener(v -> loadTodayActiveUsers());
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewActiveUsers);
        progressBar = findViewById(R.id.progressBar);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        searchView = findViewById(R.id.searchView);
        fabRefresh = findViewById(R.id.fabRefresh);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActiveUserAdapter(filteredList, user -> {});
        recyclerView.setAdapter(adapter);
    }

    private void loadTodayActiveUsers() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance()
                .getReference("analytics")
                .child(todayDate)
                .child("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        userList.clear();
                        filteredList.clear();
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String email = userSnapshot.child("email").getValue(String.class);
                            Long timestamp = userSnapshot.child("timestamp").getValue(Long.class);
                            if (email != null && timestamp != null) {
                                ActiveUser user = new ActiveUser(email, timestamp);
                                userList.add(user);
                                filteredList.add(user);
                            }
                        }
                        updateUI();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void filterUsers(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(userList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (ActiveUser user : userList) {
                if (user.getEmail().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateTotalCount();
    }

    private void updateUI() {
        adapter.notifyDataSetChanged();
        updateTotalCount();
        tvCurrentTime.setText("Last Updated: " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
    }

    private void updateTotalCount() {
        tvTotalCount.setText(String.format("Total Active: %d", filteredList.size()));
    }

    public static class ActiveUser {
        private String email;
        private long timestamp;

        public ActiveUser(String email, long timestamp) {
            this.email = email;
            this.timestamp = timestamp;
        }

        public String getEmail() {
            return email;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}