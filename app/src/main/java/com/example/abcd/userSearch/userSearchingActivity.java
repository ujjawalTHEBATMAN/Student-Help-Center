package com.example.abcd.userSearch;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.abcd.R;
import com.example.abcd.firebaseLogin.HelperClassPOJO;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class userSearchingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserSearchAdapter adapter;
    private List<HelperClassPOJO> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_searching);

        recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        adapter = new UserSearchAdapter(userList,
                user -> {
                    Intent intent = new Intent(this, UserProfileActivity.class);
                    intent.putExtra("USER_EMAIL", user.getEmail());
                    startActivity(intent);
                },
                user -> {
                    String userKey = user.getEmail().replace(".", ",");
                    FirebaseDatabase.getInstance().getReference("users")
                            .child(userKey)
                            .removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
                                loadAllUsers(); // Refresh list after deletion
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Delete failed: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                });
        recyclerView.setAdapter(adapter);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchUsers(newText);
                return true;
            }
        });

        // Load all users initially
        loadAllUsers();
    }

    private void loadAllUsers() {
        FirebaseDatabase.getInstance().getReference("users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            HelperClassPOJO user = snapshot.getValue(HelperClassPOJO.class);
                            if (user != null) {
                                userList.add(user);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (userList.isEmpty()) {
                            Toast.makeText(userSearchingActivity.this,
                                    "No users found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(userSearchingActivity.this,
                                "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void searchUsers(String searchText) {
        Query query;
        if (searchText.isEmpty()) {
            loadAllUsers();
        } else {
            query = FirebaseDatabase.getInstance().getReference("users")
                    .orderByChild("user")
                    .startAt(searchText)
                    .endAt(searchText + "\uf8ff");

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        HelperClassPOJO user = snapshot.getValue(HelperClassPOJO.class);
                        if (user != null) {
                            userList.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(userSearchingActivity.this,
                            "Search error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}