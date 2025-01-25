package com.example.abcd.fragment;

import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.view.HapticFeedbackConstants;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.abcd.R;
import com.example.abcd.SendLiveMessageActivity;
import com.example.abcd.availableAdminPage;
import com.example.abcd.models.Message;
import com.example.abcd.notificationSection.NotifictionActivity;
import com.example.abcd.selectChatModel;
import com.example.abcd.userSearch.userSearchingActivity;
import com.example.abcd.utils.SessionManager;
import com.example.abcd.firebaseLogin.HelperClassPOJO;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
    private String currentUserRole;
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


        // Add this in homeFragment's onCreateView after initializing other buttons
        ImageButton mailButton = view.findViewById(R.id.mailButton);
        mailButton.setOnClickListener(v -> {
            if (sessionManager.getEmail() != null) {
                Intent intent = new Intent(getActivity(), NotifictionActivity.class);
                intent.putExtra("USER_EMAIL", sessionManager.getEmail());
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            }
        });


        ImageButton searchButton = view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> {
            if (currentUserRole != null && currentUserRole.equals("admin")) {
                startActivity(new Intent(getActivity(), userSearchingActivity.class));
            } else {
                showPremiumAlert();
            }
        });
        ImageButton chatButton = view.findViewById(R.id.chatButton);
        chatButton.setOnClickListener(v -> {
            if (currentUserRole != null && currentUserRole.equals("admin")) {
                startActivity(new Intent(getActivity(), selectChatModel.class));
            } else {
                showPremiumAlert();
            }
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

        setupUserData();

        // Listen for new messages
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messagesList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message != null) messagesList.add(message);
                }
                adapter.notifyDataSetChanged();
                if (!messagesList.isEmpty())
                    recyclerView.smoothScrollToPosition(messagesList.size() - 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read messages", databaseError.toException());
                Toast.makeText(getContext(), "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), SendLiveMessageActivity.class)));

        return view;
    }

    private void showPremiumAlert() {
        View alertView = LayoutInflater.from(getContext()).inflate(R.layout.alert_card_modern, null);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setView(alertView)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        alertView.findViewById(R.id.btnClose).setOnClickListener(v -> {
            performHapticFeedback();
            dialog.dismiss();
        });

        alertView.findViewById(R.id.btnLearnMore).setOnClickListener(v -> {
            performHapticFeedback();
            startActivity(new Intent(requireContext(), availableAdminPage.class));
            dialog.dismiss();
        });

        Window window = dialog.getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.DialogAnimation);
        }
    }

    private void performHapticFeedback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireView().performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        } else {
            requireView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
    }

    private void setupUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HelperClassPOJO user = dataSnapshot.getValue(HelperClassPOJO.class);
                    if (user != null && user.getUser() != null) {
                        currentUserName = user.getUser();
                        currentUserRole = user.getUserRole();
                    } else {
                        Log.e(TAG, "Invalid user data");
                        showDataErrorToast();
                    }
                } else {
                    Log.e(TAG, "User data not found");
                    showDataErrorToast();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                showDataErrorToast();
            }

            private void showDataErrorToast() {
                Toast.makeText(getContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}