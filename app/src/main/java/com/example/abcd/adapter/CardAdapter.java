package com.example.abcd.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.R;

import java.util.Collections;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
    private List<Integer> cards;
    private static final String GITHUB_URL = "https://github.com/ujjawalTHEBATMAN/Student-Help-Center";

    public CardAdapter(List<Integer> cards) {
        this.cards = cards;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        holder.cardImage.setImageResource(cards.get(position));

        // Handle first card special layout
        if (position == 0) {
            holder.tvAppDescription.setVisibility(View.VISIBLE);
            holder.ivAppLogo.setVisibility(View.VISIBLE);
            holder.tvAppName.setVisibility(View.VISIBLE);
            holder.tvFeaturesTitle.setVisibility(View.VISIBLE);
            holder.tvFeaturesList.setVisibility(View.VISIBLE);
            holder.llGithub.setVisibility(View.VISIBLE);

            // Set app logo
            holder.ivAppLogo.setImageResource(R.drawable.app_logo);
            
            // Set GitHub logo
            holder.ivGithubLogo.setImageResource(R.drawable.github_logo);

            // Set GitHub link click listener
            holder.llGithub.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL));
                v.getContext().startActivity(intent);
            });
        } else {
            holder.tvAppDescription.setVisibility(View.GONE);
            holder.ivAppLogo.setVisibility(View.GONE);
            holder.tvAppName.setVisibility(View.GONE);
            holder.tvFeaturesTitle.setVisibility(View.GONE);
            holder.tvFeaturesList.setVisibility(View.GONE);
            holder.llGithub.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public void swapItems(int fromPosition, int toPosition) {
        Collections.swap(cards, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView cardImage;
        TextView tvAppDescription;
        ImageView ivAppLogo;
        TextView tvAppName;
        TextView tvFeaturesTitle;
        TextView tvFeaturesList;
        LinearLayout llGithub;
        ImageView ivGithubLogo;
        TextView tvGithubLink;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.cardImage);
            tvAppDescription = itemView.findViewById(R.id.tvAppDescription);
            ivAppLogo = itemView.findViewById(R.id.ivAppLogo);
            tvAppName = itemView.findViewById(R.id.tvAppName);
            tvFeaturesTitle = itemView.findViewById(R.id.tvFeaturesTitle);
            tvFeaturesList = itemView.findViewById(R.id.tvFeaturesList);
            llGithub = itemView.findViewById(R.id.llGithub);
            ivGithubLogo = itemView.findViewById(R.id.ivGithubLogo);
            tvGithubLink = itemView.findViewById(R.id.tvGithubLink);
        }
    }
}
