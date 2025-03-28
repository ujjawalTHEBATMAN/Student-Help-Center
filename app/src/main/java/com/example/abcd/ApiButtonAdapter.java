package com.example.abcd;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

public class ApiButtonAdapter extends RecyclerView.Adapter<ApiButtonAdapter.ApiButtonViewHolder> {

    private Context context;
    private List<Map<String, Object>> apiList;

    public ApiButtonAdapter(Context context, List<Map<String, Object>> apiList) {
        this.context = context;
        this.apiList = apiList;
    }

    @NonNull
    @Override
    public ApiButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_api_button, parent, false);
        return new ApiButtonViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ApiButtonViewHolder holder, int position) {
        Map<String, Object> api = apiList.get(position);
        String name = (String) api.get("name");
        String imageUrl = (String) api.get("image_url");

        holder.textViewApiName.setText(name);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background) // Placeholder image
                    .error(R.drawable.ic_launcher_background)       // Error image
                    .into(holder.imageViewApiIcon);
        } else {
            holder.imageViewApiIcon.setImageResource(R.drawable.ic_launcher_background); // Default image
        }

        holder.itemView.setOnClickListener(v -> {
            // Send data to MessagingActivity
            Intent intent = new Intent(context, MessagingActivity.class); // Replace MessagingActivity with your actual class name
            Gson gson = new Gson();
            String apiJson = gson.toJson(api);
            intent.putExtra("api_data", apiJson);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return apiList.size();
    }

    public static class ApiButtonViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewApiIcon;
        public TextView textViewApiName;

        public ApiButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewApiIcon = itemView.findViewById(R.id.imageViewApiIcon);
            textViewApiName = itemView.findViewById(R.id.textViewApiName);
        }
    }
}