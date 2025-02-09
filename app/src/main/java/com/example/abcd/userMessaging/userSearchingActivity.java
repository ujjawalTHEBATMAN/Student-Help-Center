package com.example.abcd.userMessaging;


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

    // For demonstration purposes, we assume the current logged-in user is available here.
    // In your real implementation, this might come from SharedPreferences, a SessionManager, or an Intent extra.
    private String currentUserEmail = "current.user@example.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_searching);

        recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();

        adapter = new UserSearchAdapter(userList,
                user -> {
                    // When a user is clicked, navigate to ChatActivity
                    Intent intent = new Intent(userSearchingActivity.this, ChatActivity.class);
                    // Pass the current user's identifier and the selected user's identifier
                    intent.putExtra("currentUser", currentUserEmail);
                    intent.putExtra("chatUser", user.getEmail());
                    startActivity(intent);
                },
                user -> {
                    // Delete user handler
                    FirebaseDatabase.getInstance().getReference("users")
                            .child(user.getEmail().replace(".", ",")) // assuming email is used as node key
                            .removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(userSearchingActivity.this, "User deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(userSearchingActivity.this, "Delete failed: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                });
        recyclerView.setAdapter(adapter);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Optional: you may hide the keyboard on submit
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchUsers(newText);
                return true;
            }
        });
    }

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
                Toast.makeText(userSearchingActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
