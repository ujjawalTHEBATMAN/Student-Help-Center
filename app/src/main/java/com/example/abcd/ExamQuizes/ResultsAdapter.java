package com.example.abcd.ExamQuizes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
    private List<QuizAttempt> attempts;
    private String userRole;

    public ResultsAdapter(List<QuizResult> results, List<QuizAttempt> attempts, String userRole) {
        this.results = results;
        this.attempts = attempts;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        if ("student".equals(userRole) && !attempts.isEmpty()) {
            QuizAttempt attempt = attempts.get(0); // One attempt per subject for simplicity
            StringBuilder details = new StringBuilder();
            int questionNumber = 1;
            for (QuestionAttempt qa : attempt.getQuestions()) {
                details.append("Q").append(questionNumber++).append(": ").append(qa.getQuestion()).append("\n")
                        .append("Your Answer: ").append(qa.getUserAnswer()).append("\n")
                        .append("Correct Answer: ").append(qa.getCorrectAnswer()).append("\n")
                        .append("Options: ").append(qa.getOptions().getOption1()).append(", ")
                        .append(qa.getOptions().getOption2()).append(", ")
                        .append(qa.getOptions().getOption3()).append(", ")
                        .append(qa.getOptions().getOption4()).append("\n\n");
            }
            holder.tvUserEmail.setText(attempt.getEmail());
            holder.marksContainer.setVisibility(View.GONE);
            holder.tvTimestamp.setText(new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    .format(new Date(results.get(0).getTimestamp())));
            holder.tvAttemptDetails.setText(details.toString());
            holder.tvAttemptDetails.setVisibility(View.VISIBLE);
        } else {
            QuizResult result = results.get(position);
            holder.tvUserEmail.setText(result.getUserEmail());
            holder.tvMarks.setText(String.format("%.1f / %.1f", result.getObtainedMarks(), result.getTotalMarks()));
            holder.marksContainer.setVisibility(View.VISIBLE);
            holder.tvTimestamp.setText(new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    .format(new Date(result.getTimestamp())));
            holder.tvAttemptDetails.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return "student".equals(userRole) && !attempts.isEmpty() ? 1 : results.size();
    }

    static class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserEmail, tvMarks, tvTimestamp, tvAttemptDetails;
        LinearLayout marksContainer;

        ResultViewHolder(View itemView) {
            super(itemView);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvMarks = itemView.findViewById(R.id.tvMarks);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvAttemptDetails = itemView.findViewById(R.id.tvAttemptDetails);
            marksContainer = itemView.findViewById(R.id.marksContainer);
        }
    }
}