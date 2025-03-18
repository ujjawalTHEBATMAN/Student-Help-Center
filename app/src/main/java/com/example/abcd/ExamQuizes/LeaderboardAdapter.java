package com.example.abcd.ExamQuizes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.R;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<QuizResult> leaderboard;

    public LeaderboardAdapter(List<QuizResult> leaderboard) {
        this.leaderboard = leaderboard;
    }

    public void updateData(List<QuizResult> newData) {
        leaderboard = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizResult result = leaderboard.get(position);
        holder.tvRank.setText(String.valueOf(position + 1));
        holder.tvEmail.setText(result.getUserEmail());
        holder.tvScore.setText(String.format("%.1f", result.getObtainedMarks()));
    }

    @Override
    public int getItemCount() {
        return leaderboard.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvEmail, tvScore;

        ViewHolder(View view) {
            super(view);
            tvRank = view.findViewById(R.id.tv_rank);
            tvEmail = view.findViewById(R.id.tv_email);
            tvScore = view.findViewById(R.id.tv_score);
        }
    }
}