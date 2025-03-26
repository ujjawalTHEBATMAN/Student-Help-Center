package com.example.abcd;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.firebaseLogin.HelperClassPOJO;
import com.example.abcd.userMessaging.ChatActivity;
import com.example.abcd.userMessaging.UserSearchAdapter;
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
    private String currentUserEmail = "current.user@example.com";  // Replace with the actual logged-in user email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_searching);  // Ensure you use the correct layout file

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();

        // Initialize Adapter with Click and Delete Handlers
        adapter = new UserSearchAdapter(userList,
                user -> {
                    // Navigate to ChatActivity when a user is clicked
                    Intent intent = new Intent(selectChatModel.this, ChatActivity.class);
                    intent.putExtra("currentUser", currentUserEmail);
                    intent.putExtra("chatUser", user.getEmail());
                    startActivity(intent);
                },
                user -> {
                    // Delete user handler
                    FirebaseDatabase.getInstance().getReference("users")
                            .child(user.getEmail().replace(".", ","))
                            .removeValue()
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(selectChatModel.this, "User deleted", Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(selectChatModel.this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                });

        recyclerView.setAdapter(adapter);

        // Search View Implementation
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;  // No action on submit
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchUsers(newText);
                return true;
            }
        });

        // Load all users initially
        searchUsers("");
    }

    // Firebase Query to Search Users
    private void searchUsers(String searchText) {
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
                    if (user != null) {
                        userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(selectChatModel.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
