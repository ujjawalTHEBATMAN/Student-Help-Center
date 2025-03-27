package com.example.abcd.ExamQuizes;

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

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ResultViewHolder> {

    private List<QuizResult> results;

    public ResultsAdapter(List<QuizResult> results) {
        this.results = results;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        QuizResult result = results.get(position);
        holder.tvUserEmail.setText(result.getUserEmail());
        holder.tvMarks.setText(String.format("%.1f / %.1f", result.getObtainedMarks(), result.getTotalMarks()));
        holder.tvTimestamp.setText(new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                .format(new Date(result.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserEmail, tvMarks, tvTimestamp;

        ResultViewHolder(View itemView) {
            super(itemView);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvMarks = itemView.findViewById(R.id.tvMarks);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}