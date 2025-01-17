package com.example.abcd;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_messageai, parent, false); // Updated layout name
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        if ("user".equals(message.getSender())) {
            holder.textViewUserMessage.setVisibility(View.VISIBLE);
            holder.textViewAIMessage.setVisibility(View.GONE);
            holder.textViewUserMessage.setText(message.getContent());
        } else {
            holder.textViewUserMessage.setVisibility(View.GONE);
            holder.textViewAIMessage.setVisibility(View.VISIBLE);
            holder.textViewAIMessage.setText(message.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUserMessage;
        TextView textViewAIMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUserMessage = itemView.findViewById(R.id.textViewUserMessage);
            textViewAIMessage = itemView.findViewById(R.id.textViewAIMessage);
        }
    }
}
