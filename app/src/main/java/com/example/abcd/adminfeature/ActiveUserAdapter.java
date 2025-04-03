package com.example.abcd.adminfeature;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActiveUserAdapter extends RecyclerView.Adapter<ActiveUserAdapter.UserViewHolder> {

    private List<TotalActivityToday.ActiveUser> userList;
    private OnItemClickListener clickListener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(TotalActivityToday.ActiveUser user);
    }

    public ActiveUserAdapter(List<TotalActivityToday.ActiveUser> userList, OnItemClickListener listener) {
        this.userList = userList;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_active_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        TotalActivityToday.ActiveUser user = userList.get(position);
        holder.tvEmail.setText(user.getEmail());

        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                .format(new Date(user.getTimestamp()));
        holder.tvTimestamp.setText("Last Active: " + time);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(user);
            }

            // Start feature graph activity
            Intent intent = new Intent(context, FeatureUsageDetailsActivity.class);
            intent.putExtra("USER_EMAIL", user.getEmail());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail, tvTimestamp;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}