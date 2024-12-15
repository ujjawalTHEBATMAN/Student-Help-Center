package com.example.abcd.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.R;
import com.example.abcd.firebaseLogin.HelperClassPOJO;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<HelperClassPOJO> userList;

    public UserAdapter(List<HelperClassPOJO> userList) {
        this.userList = userList;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        HelperClassPOJO user = userList.get(position);
        holder.usernameText.setText(user.getUser());
        holder.emailText.setText(user.getEmail());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        public TextView usernameText, emailText;

        public UserViewHolder(View view) {
            super(view);
            usernameText = view.findViewById(R.id.usernameText);
            emailText = view.findViewById(R.id.emailText);
        }
    }
}
