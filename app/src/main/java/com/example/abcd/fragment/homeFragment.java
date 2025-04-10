package com.example.abcd.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.R;
import com.example.abcd.SendLiveMessageActivity;
import com.example.abcd.availableAdminPage;
import com.example.abcd.adminfeature.Notification.viewAdminNotification;
import com.example.abcd.models.Message;
import com.example.abcd.utils.SessionManager;
import com.example.abcd.firebaseLogin.HelperClassPOJO;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class homeFragment extends Fragment implements HomeAdapter.OnItemLongClickListener {

    private RecyclerView recyclerView;
    private String currentUserRole;
    private DatabaseReference messagesRef, userRef, analyticsRef;
    private List<Message> messagesList;
    private HomeAdapter adapter;
    private SessionManager sessionManager;
    private ImageView notificationIcon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        sessionManager = new SessionManager(requireContext());
        String userEmail = sessionManager.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(getContext(), "Please sign in to continue", Toast.LENGTH_LONG).show();
            return view;
        }

        recyclerView = view.findViewById(R.id.recyclerView);
        notificationIcon = view.findViewById(R.id.notification_icon);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messagesList = new ArrayList<>();
        adapter = new HomeAdapter(messagesList, false);
        adapter.setOnItemLongClickListener(this);
        recyclerView.setAdapter(adapter);

        messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        String sanitizedEmail = userEmail.replace(".", ",");
        userRef = FirebaseDatabase.getInstance().getReference("users").child(sanitizedEmail);
        analyticsRef = FirebaseDatabase.getInstance().getReference("analytics");

        setupUserData();
        loadMessages();
        trackPageView(sanitizedEmail);
        setupNotificationIcon();

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> startActivity(new Intent(getActivity(), SendLiveMessageActivity.class)));
        return view;
    }

    private void setupNotificationIcon() {
        notificationIcon.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), viewAdminNotification.class);
            startActivity(intent);
        });
    }

    private void loadMessages() {
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
                if (!messagesList.isEmpty()) {
                    recyclerView.smoothScrollToPosition(messagesList.size() - 1);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void trackPageView(String sanitizedEmail) {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DatabaseReference dateRef = analyticsRef.child(currentDate);

        dateRef.child("users").child(sanitizedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Map<String, Object> analyticsData = new HashMap<>();

                    dateRef.child("totalAppViews").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot viewSnapshot) {
                            long currentViews = viewSnapshot.exists() ? viewSnapshot.getValue(Long.class) : 0;
                            analyticsData.put("totalAppViews", currentViews + 1);

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", sessionManager.getEmail());
                            userData.put("timestamp", System.currentTimeMillis());
                            analyticsData.put("users/" + sanitizedEmail, userData);

                            dateRef.updateChildren(analyticsData)
                                    .addOnSuccessListener(aVoid -> {})
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Failed to update analytics", Toast.LENGTH_SHORT).show();
                                    });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Error reading views: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error checking user visit: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditClicked(int position) {
        Message message = messagesList.get(position);
        String currentUserEmail = sessionManager.getEmail();
        boolean isAdmin = currentUserRole != null && currentUserRole.equalsIgnoreCase("admin");
        boolean isOwner = currentUserEmail != null && currentUserEmail.equalsIgnoreCase(message.getUserName());
        if (!isAdmin && !isOwner) {
            Toast.makeText(getContext(), "Action not allowed", Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select an Action")
                .setMessage("Do you want to edit or delete this message?")
                .setPositiveButton("Edit", (dialog, which) -> showEditDialog(message, position))
                .setNegativeButton("Delete", (dialog, which) -> deleteMessage(message, position))
                .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showEditDialog(Message message, int position) {
        View editView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_message, null);
        EditText editMessageText = editView.findViewById(R.id.editMessageText);
        editMessageText.setText(message.getMessageText());
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(editView)
                .setTitle("Edit Message")
                .setPositiveButton("Update", (dialogInterface, i) -> {
                    String updatedMessage = editMessageText.getText().toString().trim();
                    if (!updatedMessage.isEmpty()) {
                        DatabaseReference messageRef = messagesRef.child(String.valueOf(message.getTimestamp()));
                        messageRef.child("messageText").setValue(updatedMessage)
                                .addOnSuccessListener(aVoid -> {
                                    message.setMessageText(updatedMessage);
                                    adapter.notifyItemChanged(position);
                                    Toast.makeText(getContext(), "Message updated", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .create();
        dialog.show();
    }

    private void deleteMessage(Message message, int position) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    messagesRef.child(String.valueOf(message.getTimestamp())).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                if (position >= 0 && position < messagesList.size()) {
                                    messagesList.remove(position);
                                    if (messagesList.isEmpty()) {
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        adapter.notifyItemRemoved(position);
                                    }
                                    Toast.makeText(getContext(), "Message deleted successfully", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete message", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onDeleteClicked(int position) {
        Message message = messagesList.get(position);
        String currentUserEmail = sessionManager.getEmail();
        boolean isAdmin = currentUserRole != null && currentUserRole.equalsIgnoreCase("admin");
        boolean isOwner = currentUserEmail != null && currentUserEmail.equalsIgnoreCase(message.getUserName());
        if (!isAdmin && !isOwner) {
            Toast.makeText(getContext(), "Action not allowed", Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    messagesRef.child(String.valueOf(message.getTimestamp())).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                messagesList.remove(position);
                                adapter.notifyItemRemoved(position);
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HelperClassPOJO user = dataSnapshot.getValue(HelperClassPOJO.class);
                currentUserRole = user != null ? user.getUserRole() : null;
                adapter.setAdmin("admin".equalsIgnoreCase(currentUserRole));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserRoleAndNavigate(Class<?> targetActivity) {
        String userEmail = sessionManager.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(getContext(), "Please sign in to continue", Toast.LENGTH_SHORT).show();
            return;
        }
        String sanitizedEmail = userEmail.replace(".", ",");
        FirebaseDatabase.getInstance().getReference("users").child(sanitizedEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        HelperClassPOJO user = snapshot.getValue(HelperClassPOJO.class);
                        String role = user != null ? user.getUserRole() : "";
                        if ("admin".equalsIgnoreCase(role)) {
                            startActivity(new Intent(getActivity(), targetActivity));
                        } else {
                            showPremiumAlert();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error checking user role", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPremiumAlert() {
        startActivity(new Intent(requireContext(), availableAdminPage.class));
    }
}