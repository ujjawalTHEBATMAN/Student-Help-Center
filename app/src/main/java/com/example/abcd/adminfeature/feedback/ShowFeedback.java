package com.example.abcd.adminfeature.feedback;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.abcd.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowFeedback extends AppCompatActivity {

    private RecyclerView feedbackRecyclerView;
    private ProgressBar loadingProgressBar;
    private CoordinatorLayout rootLayout;
    private DatabaseReference feedbackRef;
    private FeedbackAdapter feedbackAdapter;
    private List<FeedbackItem> feedbackList;
    private Map<String, String> feedbackKeys;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialToolbar toolbar;
    private FloatingActionButton fabAddFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_feedback);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        feedbackRecyclerView = findViewById(R.id.feedback_recycler_view);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        rootLayout = findViewById(R.id.root_layout);
        fabAddFeedback = findViewById(R.id.fab_add_feedback);

        feedbackRef = FirebaseDatabase.getInstance().getReference("feedback");
        feedbackList = new ArrayList<>();
        feedbackKeys = new HashMap<>();
        feedbackAdapter = new FeedbackAdapter(feedbackList);

        setupRecyclerView();
        fetchFeedback();
        setupListeners();
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::fetchFeedback);
        fabAddFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(ShowFeedback.this, FeedbackCreation.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void setupRecyclerView() {
        feedbackRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedbackRecyclerView.setAdapter(feedbackAdapter);
    }

    private void fetchFeedback() {
        swipeRefreshLayout.setRefreshing(true);
        loadingProgressBar.setVisibility(View.GONE);
        feedbackRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                feedbackList.clear();
                feedbackKeys.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    String title = snapshot.child("title").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String timestamp = snapshot.child("timestamp").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);

                    FeedbackItem item = new FeedbackItem(title, description, timestamp, status);
                    feedbackList.add(item);
                    feedbackKeys.put(title + timestamp, key);
                }
                feedbackAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                swipeRefreshLayout.setRefreshing(false);
                showSnackbar("Failed to load feedback: " + databaseError.getMessage());
            }
        });
    }

    private void showRemoveDialog(int position) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remove Feedback")
                .setMessage("Would you like to remove this feedback item?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    removeFeedback(position);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void removeFeedback(int position) {
        if (position >= 0 && position < feedbackList.size()) {
            FeedbackItem item = feedbackList.get(position);
            String uniqueIdentifier = item.title + item.timestamp;
            String key = feedbackKeys.get(uniqueIdentifier);

            if (key != null) {
                feedbackRef.child(key).removeValue()
                        .addOnSuccessListener(aVoid -> showSnackbar("Feedback removed successfully"))
                        .addOnFailureListener(e -> showSnackbar("Failed to remove feedback: " + e.getMessage()));
            } else {
                showSnackbar("Could not find the feedback item key");
            }
        }
    }

    private void showSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundTintList(ContextCompat.getColorStateList(this, com.google.android.material.R.color.design_default_color_primary_dark));
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        snackbar.show();
    }

    private static class FeedbackItem {
        String title, description, timestamp, status;

        FeedbackItem(String title, String description, String timestamp, String status) {
            this.title = title;
            this.description = description;
            this.timestamp = timestamp;
            this.status = status;
        }
    }

    private class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

        private List<FeedbackItem> feedbackItems;

        FeedbackAdapter(List<FeedbackItem> feedbackItems) {
            this.feedbackItems = feedbackItems;
        }

        @Override
        public FeedbackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_feedback, parent, false);
            return new FeedbackViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FeedbackViewHolder holder, int position) {
            FeedbackItem item = feedbackItems.get(position);
            holder.titleTextView.setText(item.title);
            holder.descriptionTextView.setText(item.description);
            holder.timestampTextView.setText(item.timestamp);
            holder.statusTextView.setText(item.status);

            // Set long click listener for each item
            holder.itemView.setOnLongClickListener(v -> {
                showRemoveDialog(holder.getAdapterPosition());
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return feedbackItems.size();
        }

        class FeedbackViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView, descriptionTextView, timestampTextView, statusTextView;

            FeedbackViewHolder(View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.feedback_title_textview);
                descriptionTextView = itemView.findViewById(R.id.feedback_description_textview);
                timestampTextView = itemView.findViewById(R.id.feedback_timestamp_textview);
                statusTextView = itemView.findViewById(R.id.feedback_status_textview);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}