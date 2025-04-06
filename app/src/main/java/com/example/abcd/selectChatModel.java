package com.example.abcd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.firebaseLogin.HelperClassPOJO;
import com.example.abcd.userMessaging.ChatActivity;
import com.example.abcd.userMessaging.UserSearchAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
public class selectChatModel extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UserSearchAdapter adapter;
    private List<HelperClassPOJO> userList;
    private ProgressBar progressBar;
    private String currentUserEmail = "current.user@example.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_searching);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Profile button
        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> {
            // Handle profile click - maybe start ProfileActivity
        });

        // RecyclerView setup
        recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        adapter = new UserSearchAdapter(userList, this::startChat, this::deleteUser);
        recyclerView.setAdapter(adapter);

        // ProgressBar
        progressBar = findViewById(R.id.progressBar);

        // SearchView
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchUsers(newText);
                return true;
            }
        });

        // FAB
        FloatingActionButton fabRefresh = findViewById(R.id.fabRefresh);
        fabRefresh.setOnClickListener(v -> {
            searchUsers("");
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
        });

        searchUsers("");
    }

    private void startChat(HelperClassPOJO user) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("currentUser", currentUserEmail);
        intent.putExtra("chatUser", user.getEmail());
        startActivity(intent);
    }

    private void deleteUser(HelperClassPOJO user) {
        // Delete logic remains same
    }

    private void searchUsers(String searchText) {
        progressBar.setVisibility(View.VISIBLE);
        Query query = FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("user")
                .startAt(searchText)
                .endAt(searchText + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HelperClassPOJO user = snapshot.getValue(HelperClassPOJO.class);
                    if (user != null && !user.getEmail().equals(currentUserEmail)) {
                        userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(selectChatModel.this, "Failed: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}