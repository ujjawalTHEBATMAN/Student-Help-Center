package com.example.abcd.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.R;
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

public class treatMeWellFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserSearchAdapter adapter;
    private List<HelperClassPOJO> userList;
    private String currentUserEmail = "current.user@example.com";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the new layout (updated from activity_user_searching.xml)
        View view = inflater.inflate(R.layout.fragment_treat_me_well, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();

        adapter = new UserSearchAdapter(userList,
                user -> {
                    // When a user is clicked, navigate to ChatActivity
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("currentUser", currentUserEmail);
                    intent.putExtra("chatUser", user.getEmail());
                    startActivity(intent);
                },
                user -> {
                    // Delete user handler
                    FirebaseDatabase.getInstance().getReference("users")
                            .child(user.getEmail().replace(".", ","))
                            .removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "User deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Delete failed: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                });
        recyclerView.setAdapter(adapter);

        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Optionally hide the keyboard on submit
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchUsers(newText);
                return true;
            }
        });

        return view;
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
                Toast.makeText(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}