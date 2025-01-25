package com.example.abcd.userSearch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.abcd.R;
import com.example.abcd.firebaseLogin.HelperClassPOJO;
import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {

    private final List<HelperClassPOJO> userList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(HelperClassPOJO user);
    }

    public UserSearchAdapter(List<HelperClassPOJO> userList, OnItemClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        HelperClassPOJO user = userList.get(position);

        // Load profile image
        if (user.getImageSend() != null && !user.getImageSend().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getImageSend())
                    .placeholder(R.drawable.ic_default_profile)
                    .error(R.drawable.ic_default_profile)
                    .into(holder.ivUserProfile);
        } else {
            holder.ivUserProfile.setImageResource(R.drawable.ic_default_profile);
        }

        // Set user data
        holder.tvUserName.setText(user.getUser());
        holder.tvUserEmail.setText(user.getEmail());

        // Set click listener
        holder.itemView.setOnClickListener(v -> listener.onItemClick(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserProfile;
        TextView tvUserName, tvUserEmail;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserProfile = itemView.findViewById(R.id.ivUserProfile);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
        }
    }
}