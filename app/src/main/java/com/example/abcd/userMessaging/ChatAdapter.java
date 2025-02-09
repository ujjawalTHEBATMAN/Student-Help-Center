package com.example.abcd.userMessaging;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.R;
import com.example.abcd.userMessaging.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 0;
    private static final int VIEW_TYPE_RECEIVED = 1;
    private ArrayList<ChatMessage> chatMessages;
    private String currentUser;

    public ChatAdapter(ArrayList<ChatMessage> chatMessages, String currentUser) {
        this.chatMessages = chatMessages;
        this.currentUser = currentUser;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);
        return message.getSender().equals(currentUser) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            // Inflate SENT message layout
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false); // Use sent layout
            return new SentMessageHolder(view);
        } else {
            // Inflate RECEIVED message layout
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false); // Use received layout
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        if(holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageHolder)holder).bind(message);
        } else {
            ((ReceivedMessageHolder)holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    // ViewHolder for sent messages
    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime;

        SentMessageHolder(View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessageSent);
            txtTime = itemView.findViewById(R.id.txtTimeSent);
        }

        void bind(ChatMessage message) {
            txtMessage.setText(message.getMessage());
            txtTime.setText(getFormattedTime(message.getTimestamp()));
        }
    }

    // ViewHolder for received messages
    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessageReceived);
            txtTime = itemView.findViewById(R.id.txtTimeReceived);
        }

        void bind(ChatMessage message) {
            txtMessage.setText(message.getMessage());
            txtTime.setText(getFormattedTime(message.getTimestamp()));
        }
    }

    private String getFormattedTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // Optionally, add helper methods to update the list
    public void addMessage(ChatMessage message) {
        chatMessages.add(message);
        notifyItemInserted(chatMessages.size() - 1);
    }
}

