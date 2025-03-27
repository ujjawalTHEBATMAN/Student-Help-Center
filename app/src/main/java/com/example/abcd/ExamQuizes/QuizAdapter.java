package com.example.abcd.ExamQuizes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.abcd.R;
import com.example.abcd.databinding.ItemQuizBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {

    interface QuizInteractionListener {
        void onQuizClicked(Quiz quiz);
        void onQuizLongClicked(Quiz quiz);
    }

    private final List<Quiz> quizList;
    private final QuizInteractionListener listener;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    public QuizAdapter(List<Quiz> quizList, QuizInteractionListener listener) {
        this.quizList = quizList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemQuizBinding binding = ItemQuizBinding.inflate(inflater, parent, false);
        return new QuizViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        Quiz quiz = quizList.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.tvSubject.setText(quiz.getSubject());
        holder.binding.tvDuration.setText(context.getString(
                R.string.duration_format, quiz.getDurationMinutes()));

        int questionCount = (quiz.getQuestions() != null) ? quiz.getQuestions().size() : 0;
        holder.binding.tvQuestions.setText(context.getString(
                R.string.questions_format, questionCount));

        updateQuizStatusUI(holder, quiz, context);
        setupClickListeners(holder, quiz, context);
    }

    private void updateQuizStatusUI(QuizViewHolder holder, Quiz quiz, Context context) {
        long currentTime = System.currentTimeMillis();
        if (currentTime > quiz.getEndingTime()) {
            setStatus(holder, "Expired", R.color.red);
        } else if (currentTime >= quiz.getStartingTime()) {
            setStatus(holder, "Active", R.color.green);
        } else {
            setStatus(holder, "Starts: " + dateFormat.format(
                    new Date(quiz.getStartingTime())), R.color.blue);
        }
    }

    private void setStatus(QuizViewHolder holder, String text, int colorRes) {
        holder.binding.tvStatus.setText(text);
        holder.binding.tvStatus.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), colorRes));
    }

    private void setupClickListeners(QuizViewHolder holder, Quiz quiz, Context context) {
        holder.itemView.setOnClickListener(v -> listener.onQuizClicked(quiz));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onQuizLongClicked(quiz);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        final ItemQuizBinding binding;

        QuizViewHolder(ItemQuizBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}