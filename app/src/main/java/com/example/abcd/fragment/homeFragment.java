package com.example.abcd.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.abcd.R;
import com.example.abcd.SendLiveMessageActivity;
import com.example.abcd.adminfeature.MessageEditViewAdmin;
import com.example.abcd.availableAdminPage;
import com.example.abcd.models.Message;
import com.example.abcd.notificationSection.NotifictionActivity;
import com.example.abcd.selectChatModel;
import com.example.abcd.userSearch.userSearchingActivity;
import com.example.abcd.utils.SessionManager;
import com.example.abcd.firebaseLogin.HelperClassPOJO;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class homeFragment extends Fragment implements HomeAdapter.OnItemLongClickListener {
    private RecyclerView recyclerView;
    private String currentUserRole;
    private DatabaseReference messagesRef;
    private DatabaseReference userRef;
    private List<Message> messagesList;
    private HomeAdapter adapter;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
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


        sessionManager = new SessionManager(requireContext());
        String userEmail = sessionManager.getEmail();

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(getContext(), "Please sign in to continue", Toast.LENGTH_LONG).show();
            return view;
        }

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messagesList = new ArrayList<>();
        adapter = new HomeAdapter(messagesList, false);
        adapter.setOnItemLongClickListener(this);
        recyclerView.setAdapter(adapter);

        messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        String sanitizedEmail = userEmail.replace(".", ",");
        userRef = FirebaseDatabase.getInstance().getReference("users").child(sanitizedEmail);

        setupUserData();

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
                Toast.makeText(getContext(), "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> startActivity(new Intent(getActivity(), SendLiveMessageActivity.class)));

        return view;
    }


    @Override
    public void onEditClicked(int position) {
        Message message = messagesList.get(position);

        // Show a dialog with Edit and Delete options
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
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show());
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
                                        adapter.notifyDataSetChanged();  // Refresh RecyclerView when empty
                                    } else {
                                        adapter.notifyItemRemoved(position);
                                    }
                                    Toast.makeText(getContext(), "Message deleted successfully", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed to delete message", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }




    @Override
    public void onDeleteClicked(int position) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Message message = messagesList.get(position);
                    messagesRef.child(String.valueOf(message.getTimestamp())).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                messagesList.remove(position);
                                adapter.notifyItemRemoved(position);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HelperClassPOJO user = dataSnapshot.getValue(HelperClassPOJO.class);
                    if (user != null) {
                        currentUserRole = user.getUserRole();
                        adapter.setAdmin("admin".equals(currentUserRole));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        });
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

}