package com.example.abcd.ExamQuizes;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.R;

import java.util.List;

public class QuizAnalysisAdapter extends RecyclerView.Adapter<QuizAnalysisAdapter.QuizViewHolder> {

    private List<QuizQuestion1> quizQuestions1;

    public QuizAnalysisAdapter(List<QuizQuestion1> quizQuestions1) {
        this.quizQuestions1 = quizQuestions1;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_analysis, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        QuizQuestion1 question = quizQuestions1.get(position);

        holder.tvQuestionNumber.setText("Question " + (position + 1));
        holder.tvQuestion.setText(question.getQuestion());
        holder.tvUserAnswer.setText(question.getUserAnswer());
        holder.tvCorrectAnswer.setText(question.getCorrectAnswer());

        // Set background colors based on correctness
        if (question.isCorrect()) {
            holder.layoutUserAnswer.setBackgroundColor(Color.parseColor("#C8E6C9")); // Light green
        } else {
            holder.layoutUserAnswer.setBackgroundColor(Color.parseColor("#FFCDD2")); // Light red
        }
    }

    @Override
    public int getItemCount() {
        return quizQuestions1.size();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestionNumber, tvQuestion, tvUserAnswer, tvCorrectAnswer;
        LinearLayout layoutUserAnswer, layoutCorrectAnswer;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionNumber = itemView.findViewById(R.id.tvQuestionNumber);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvUserAnswer = itemView.findViewById(R.id.tvUserAnswer);
            tvCorrectAnswer = itemView.findViewById(R.id.tvCorrectAnswer);
            layoutUserAnswer = itemView.findViewById(R.id.layoutUserAnswer);
            layoutCorrectAnswer = itemView.findViewById(R.id.layoutCorrectAnswer);
        }
    }
}