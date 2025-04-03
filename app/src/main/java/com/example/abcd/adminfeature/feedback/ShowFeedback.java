package com.example.abcd.adminfeature.feedback;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShowFeedback extends AppCompatActivity {

    private RecyclerView feedbackRecyclerView;
    private ProgressBar loadingProgressBar;
    private LinearLayout rootLayout;
    private DatabaseReference feedbackRef;
    private FeedbackAdapter feedbackAdapter;
    private List<FeedbackItem> feedbackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_feedback);

        feedbackRecyclerView = findViewById(R.id.feedback_recycler_view);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        rootLayout = findViewById(R.id.root_layout);

        feedbackRef = FirebaseDatabase.getInstance().getReference("feedback");
        feedbackList = new ArrayList<>();
        feedbackAdapter = new FeedbackAdapter(feedbackList);

        setupRecyclerView();
        fetchFeedback();
    }

    private void setupRecyclerView() {
        feedbackRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedbackRecyclerView.setAdapter(feedbackAdapter);
    }

    private void fetchFeedback() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        feedbackRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                feedbackList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String title = snapshot.child("title").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String timestamp = snapshot.child("timestamp").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    feedbackList.add(new FeedbackItem(title, description, timestamp, status));
                }
                feedbackAdapter.notifyDataSetChanged();
                loadingProgressBar.setVisibility(View.GONE);
                applyRecyclerViewAnimation();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                loadingProgressBar.setVisibility(View.GONE);
                showSnackbar("Failed to load feedback: " + databaseError.getMessage());
            }
        });
    }

    private void applyRecyclerViewAnimation() {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(feedbackRecyclerView, "alpha", 0f, 1f);
        ObjectAnimator slideUp = ObjectAnimator.ofFloat(feedbackRecyclerView, "translationY", 100f, 0f);
        animatorSet.playTogether(fadeIn, slideUp);
        animatorSet.setDuration(800);
        animatorSet.start();
    }

    private void showSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_container));
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, R.color.on_primary_container));
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

            GradientDrawable cardBackground = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{
                            ContextCompat.getColor(ShowFeedback.this, R.color.gradient_start),
                            ContextCompat.getColor(ShowFeedback.this, R.color.gradient_end)
                    }
            );
            cardBackground.setCornerRadius(16f);
            holder.cardView.setBackground(cardBackground);
        }

        @Override
        public int getItemCount() {
            return feedbackItems.size();
        }

        class FeedbackViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView, descriptionTextView, timestampTextView, statusTextView;
            CardView cardView;

            FeedbackViewHolder(View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.feedback_title_textview);
                descriptionTextView = itemView.findViewById(R.id.feedback_description_textview);
                timestampTextView = itemView.findViewById(R.id.feedback_timestamp_textview);
                statusTextView = itemView.findViewById(R.id.feedback_status_textview);
                cardView = itemView.findViewById(R.id.feedback_item_card);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}