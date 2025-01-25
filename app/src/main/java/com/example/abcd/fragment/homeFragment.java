package com.example.abcd.fragment;

import android.os.Bundle;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.abcd.R;
import com.example.abcd.SendLiveMessageActivity;
import com.example.abcd.chataiwithdistilgpt2;
import com.example.abcd.models.Message;
import com.example.abcd.selectChatModel;
import com.example.abcd.userSearch.userSearchingActivity;
import com.example.abcd.utils.SessionManager;
import com.example.abcd.firebaseLogin.HelperClassPOJO;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class homeFragment extends Fragment {
    private static final String TAG = "homeFragment";
    private RecyclerView recyclerView;
    private DatabaseReference messagesRef;
    private DatabaseReference userRef;
    private List<Message> messagesList;
    private HomeAdapter adapter;
    private SessionManager sessionManager;
    private String currentUserName;
    private FloatingActionButton fab;

    public homeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        /// onclick listener to nav to chat ai ok
        ImageButton chatButton = view.findViewById(R.id.chatButton);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to start model selector activity
                Intent intent = new Intent(getActivity(), selectChatModel.class );
                startActivity(intent);
            }
        });
        ImageButton searchButton = view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> {
            Intent searchIntent = new Intent(getActivity(), userSearchingActivity.class);
            startActivity(searchIntent);
        });


        // Initialize SessionManager
        sessionManager = new SessionManager(requireContext());
        String userEmail = sessionManager.getEmail();

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(getContext(), "Please sign in to continue", Toast.LENGTH_LONG).show();
            return view;
        }

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messagesList = new ArrayList<>();
        adapter = new HomeAdapter(messagesList);
        recyclerView.setAdapter(adapter);

        // Initialize Firebase Database references
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        String sanitizedEmail = userEmail.replace(".", ",");
        userRef = FirebaseDatabase.getInstance().getReference("users").child(sanitizedEmail);

        // Get user data and setup message sending
        setupUserData();

        // Listen for new messages
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messagesList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message != null) {
                        messagesList.add(message);
                    }
                }
                adapter.notifyDataSetChanged();
                if (messagesList.size() > 0) {
                    recyclerView.smoothScrollToPosition(messagesList.size() - 1);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read messages", databaseError.toException());
                Toast.makeText(getContext(), "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SendLiveMessageActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void setupUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HelperClassPOJO user = dataSnapshot.getValue(HelperClassPOJO.class);
                    if (user != null && user.getUser() != null) {
                        currentUserName = user.getUser();
                    } else {
                        Log.e(TAG, "User data is null or username is null");
                        Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "User data does not exist");
                    Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read user data", databaseError.toException());
                Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}