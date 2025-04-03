package com.example.abcd.adminfeature.Notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class viewAdminNotification extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<NotificationModel> notificationList;
    private ProgressBar progressBar;
    private LinearLayout rootLayout;
    private ImageView backButton;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_admin_notification);

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        rootLayout = findViewById(R.id.root_layout);
        backButton = findViewById(R.id.back_button);

        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(notificationAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("notifications");

        setupUI();
        setupListeners();
        fetchNotifications();
    }

    private void setupUI() {
        rootLayout.setBackgroundColor(getResources().getColor(R.color.background_color));
        progressBar.getIndeterminateDrawable().setColorFilter(
                getResources().getColor(R.color.colorAccent),
                android.graphics.PorterDuff.Mode.SRC_IN);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
    }

    private void fetchNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                notificationList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    NotificationModel notification = snapshot.getValue(NotificationModel.class);
                    if (notification != null) {
                        notificationList.add(notification);
                    }
                }
                notificationAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(viewAdminNotification.this, "Failed to load notifications: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

        private List<NotificationModel> notifications;

        public NotificationAdapter(List<NotificationModel> notifications) {
            this.notifications = notifications;
        }

        @Override
        public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new NotificationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(NotificationViewHolder holder, int position) {
            NotificationModel notification = notifications.get(position);
            holder.titleTextView.setText(notification.getTitle());
            holder.messageTextView.setText(notification.getMessage());
            holder.categoryTextView.setText(notification.getCategory());
            holder.timestampTextView.setText(notification.getTimestamp());
            holder.statusTextView.setText(notification.getStatus());
            holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.card_background));
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        public class NotificationViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView;
            TextView messageTextView;
            TextView categoryTextView;
            TextView timestampTextView;
            TextView statusTextView;
            CardView cardView;

            public NotificationViewHolder(View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.title_text_view);
                messageTextView = itemView.findViewById(R.id.message_text_view);
                categoryTextView = itemView.findViewById(R.id.category_text_view);
                timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
                statusTextView = itemView.findViewById(R.id.status_text_view);
                cardView = itemView.findViewById(R.id.notification_card);
            }
        }
    }

    public static class NotificationModel {
        private String title;
        private String message;
        private String category;
        private String timestamp;
        private String status;

        public NotificationModel() {}

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}