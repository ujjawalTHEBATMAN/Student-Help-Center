package com.example.abcd.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.abcd.AuthenTication.regestration.Registration2CardUtils.College;
import com.example.abcd.R;

import java.util.List;

public class CollegeAdapter extends RecyclerView.Adapter<CollegeAdapter.CollegeViewHolder> {

    public interface OnCollegeSelectedListener {
        void onCollegeSelected(College college);
    }
    public interface OnCollegeLongClickListener {
        void onCollegeLongClicked(College college);
    }

    private Context context;
    private List<College> collegeList;
    private OnCollegeSelectedListener selectedListener;
    private OnCollegeLongClickListener longClickListener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public CollegeAdapter(Context context, List<College> collegeList,
                          OnCollegeSelectedListener selectedListener,
                          OnCollegeLongClickListener longClickListener) {
        this.context = context;
        this.collegeList = collegeList;
        this.selectedListener = selectedListener;
        this.longClickListener = longClickListener;
    }

    @Override
    public CollegeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recylerviewcollagesss, parent, false);
        return new CollegeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CollegeViewHolder holder, int position) {
        College college = collegeList.get(position);
        holder.tvCollegeName.setText(college.getName());
        holder.ivCollegeImage.setImageResource(R.drawable.profile1);
        holder.cardView.setCardBackgroundColor(selectedPosition == position ? Color.LTGRAY : Color.WHITE);

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            if (selectedListener != null) {
                selectedListener.onCollegeSelected(college);
            }
        });

        // admin deletion long click event
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onCollegeLongClicked(college);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return collegeList.size();
    }

    public void updateData(List<College> newCollegeList) {
        this.collegeList = newCollegeList;
        notifyDataSetChanged();
    }

    public static class CollegeViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvCollegeName;
        ImageView ivCollegeImage;

        public CollegeViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewCollege);
            tvCollegeName = itemView.findViewById(R.id.tvCollegeName);
            ivCollegeImage = itemView.findViewById(R.id.ivCollegeImage);
        }
    }
}
