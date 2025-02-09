package com.example.abcd.userMessaging;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.R;
import com.example.abcd.utils.SessionManager;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText edtMessage;
    private ImageButton btnSend;
    private ChatAdapter chatAdapter;
    private ArrayList<ChatMessage> chatMessages;
    private DatabaseReference chatRoomRef;

    private String currentUser;
    private String chatUser;
    private String roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recyclerChat);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);

        SessionManager sessionManager = new SessionManager(this);
        currentUser = sessionManager.getEmail();
        chatUser = getIntent().getStringExtra("chatUser");

        if (TextUtils.isEmpty(currentUser) || TextUtils.isEmpty(chatUser)) {
            Toast.makeText(this, "User info missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUser = currentUser.replace(".", ",");
        chatUser = chatUser.replace(".", ",");
        roomId = generateRoomId(currentUser, chatUser);

        chatRoomRef = FirebaseDatabase.getInstance().getReference("oneOnoneChats")
                .child(roomId)
                .child("messages");

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, currentUser);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        fetchMessages();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private String generateRoomId(String user1, String user2) {
        return (user1.compareTo(user2) < 0) ? user1 + "_" + user2 : user2 + "_" + user1;
    }

    private void fetchMessages() {
        chatRoomRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                ChatMessage message = snapshot.getValue(ChatMessage.class);
                if (message != null) {
                    chatMessages.add(message);
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendMessage() {
        String messageText = edtMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) return;

        ChatMessage chatMessage = new ChatMessage(
                currentUser,
                chatUser,
                messageText,
                System.currentTimeMillis()
        );

        chatRoomRef.push().setValue(chatMessage, (error, ref) -> {
            if (error != null) {
                Toast.makeText(this, "Send failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        edtMessage.setText("");
    }
}
