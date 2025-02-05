package com.example.abcd.fragment;

import android.annotation.SuppressLint;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.abcd.R;
import com.example.abcd.models.Message;
import com.example.abcd.utils.SessionManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {
    private List<Message> messagesList;
    private DatabaseReference messagesRef;
    private boolean isAdmin;
    private OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onEditClicked(int position);
        void onDeleteClicked(int position);
    }

    public HomeAdapter(List<Message> messagesList, boolean isAdmin) {
        this.messagesList = messagesList;
        this.isAdmin = isAdmin;
        this.messagesRef = FirebaseDatabase.getInstance().getReference("messages");
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
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

        holder.textUserName.setText(message.getUserName());
        holder.textMessage.setText(message.getMessageText());
        holder.textTimestamp.setText(message.getFormattedTime());

        String imageUrl = message.getImageURL();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            holder.messageImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_error)
                    .into(holder.messageImage);
        } else {
            holder.messageImage.setVisibility(View.GONE);
        }

        SessionManager sessionManager = new SessionManager(holder.itemView.getContext());
        String currentUserEmail = sessionManager.getEmail();

        if (currentUserEmail == null) {
            holder.btnLike.setEnabled(false);
            Toast.makeText(holder.itemView.getContext(),
                    "Please sign in to like messages", Toast.LENGTH_SHORT).show();
            return;
        }

        updateLikeUI(holder, message, currentUserEmail);
        holder.btnLike.setOnClickListener(v -> handleLikeClick(holder, message, currentUserEmail));

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onEditClicked(position);
                return true;
            }
            return false;
        });
    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(R.menu.context_menu_admin);
        popup.setOnMenuItemClickListener(item -> {
            if (longClickListener != null) {
                int id = item.getItemId();
                if (id == R.id.menu_edit) {
                    longClickListener.onEditClicked(position);
                    return true;
                } else if (id == R.id.menu_delete) {
                    longClickListener.onDeleteClicked(position);
                    return true;
                }
            }
            return false;
        });
        popup.show();
    }

    // Rest of the existing methods (updateLikeUI, handleLikeClick, getItemCount, ViewHolder)
    // ... [Keep all existing methods unchanged from original code] ...

    private void updateLikeUI(ViewHolder holder, Message message, String userEmail) {
        int likeCount = message.getTotalLikes();
        holder.textLikes.setText(String.valueOf(likeCount));

        boolean isLiked = message.isLikedByUser(userEmail);
        holder.btnLike.setImageResource(isLiked ? R.drawable.ic_liked : R.drawable.ic_like);

        TypedArray typedArray = holder.itemView.getContext().obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackgroundBorderless});
        holder.btnLike.setBackground(typedArray.getDrawable(0));
        typedArray.recycle();
    }

    private void handleLikeClick(ViewHolder holder, Message message, String userEmail) {
        boolean isNowLiked = message.toggleLike(userEmail);
        updateLikeUI(holder, message, userEmail);

        String messageId = String.valueOf(message.getTimestamp());
        DatabaseReference messageRef = messagesRef.child(messageId);

        messageRef.child("likedBy").setValue(message.getLikedBy())
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(e -> {
                    message.toggleLike(userEmail);
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
        TextView textUserName, textMessage, textTimestamp, textLikes;
        ImageButton btnLike;
        ImageView messageImage;

        ViewHolder(View itemView) {
            super(itemView);
            textUserName = itemView.findViewById(R.id.textUserName);
            textMessage = itemView.findViewById(R.id.textMessage);
            textTimestamp = itemView.findViewById(R.id.textTimestamp);
            textLikes = itemView.findViewById(R.id.textLikes);
            btnLike = itemView.findViewById(R.id.btnLike);
            messageImage = itemView.findViewById(R.id.messageImage);
        }
    }
}