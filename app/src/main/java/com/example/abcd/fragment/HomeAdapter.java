package com.example.abcd.fragment;

import android.annotation.SuppressLint;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.abcd.R;
import com.example.abcd.models.Message;
import com.example.abcd.utils.SessionManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {
    private List<Message> messagesList;
    private DatabaseReference messagesRef;

    public HomeAdapter(List<Message> messagesList) {
        this.messagesList = messagesList;
        this.messagesRef = FirebaseDatabase.getInstance().getReference("messages");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messagesList.get(position);
        
        // Set basic message info
        holder.textUserName.setText(message.getUserName());
        holder.textMessage.setText(message.getMessageText());
        holder.textTimestamp.setText(message.getFormattedTime());
        
        // Get current user's email
        SessionManager sessionManager = new SessionManager(holder.itemView.getContext());
        String currentUserEmail = sessionManager.getEmail();
        
        if (currentUserEmail == null) {
            holder.btnLike.setEnabled(false);
            Toast.makeText(holder.itemView.getContext(), 
                "Please sign in to like messages", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update like count and button state
        updateLikeUI(holder, message, currentUserEmail);
        
        // Set like button click listener
        holder.btnLike.setOnClickListener(v -> handleLikeClick(holder, message, currentUserEmail));
    }

    private void updateLikeUI(ViewHolder holder, Message message, String userEmail) {
        // Update like count
        int likeCount = message.getTotalLikes();
        holder.textLikes.setText(String.valueOf(likeCount));
        
        // Update like button appearance
        boolean isLiked = message.isLikedByUser(userEmail);
        holder.btnLike.setImageResource(isLiked ? R.drawable.ic_liked : R.drawable.ic_like);
        
        // Add ripple effect
        TypedArray typedArray = holder.itemView.getContext().obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackgroundBorderless});
        holder.btnLike.setBackground(typedArray.getDrawable(0));
        typedArray.recycle();
    }

    private void handleLikeClick(ViewHolder holder, Message message, String userEmail) {
        // Toggle like state
        boolean isNowLiked = message.toggleLike(userEmail);
        
        // Update UI immediately for better user experience
        updateLikeUI(holder, message, userEmail);
        
        // Update in Firebase
        String messageId = String.valueOf(message.getTimestamp());
        DatabaseReference messageRef = messagesRef.child(messageId);
        
        messageRef.child("likedBy").setValue(message.getLikedBy())
            .addOnSuccessListener(aVoid -> {
                // Like updated successfully
            })
            .addOnFailureListener(e -> {
                // Revert the UI change on failure
                message.toggleLike(userEmail); // Revert the like
                updateLikeUI(holder, message, userEmail);
                Toast.makeText(holder.itemView.getContext(),
                    "Failed to update like", Toast.LENGTH_SHORT).show();
            });
        
        messageRef.child("totalLikes").setValue(message.getTotalLikes());
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textUserName, textMessage, textLikes, textTimestamp;
        ImageButton btnLike;

        @SuppressLint("WrongViewCast")
        ViewHolder(View itemView) {
            super(itemView);
            textUserName = itemView.findViewById(R.id.textUserName);
            textMessage = itemView.findViewById(R.id.textMessage);
            textLikes = itemView.findViewById(R.id.textLikes);
            textTimestamp = itemView.findViewById(R.id.textTimestamp);
            btnLike = itemView.findViewById(R.id.btnLike);
        }
    }
}
