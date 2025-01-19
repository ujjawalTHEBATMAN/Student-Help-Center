package com.example.abcd;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class StorageAdapter extends RecyclerView.Adapter<StorageAdapter.FileViewHolder> {
    private List<StorageFileModel> files;
    private Context context;

    public StorageAdapter(Context context) {
        this.context = context;
        this.files = new ArrayList<>();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_storage_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        StorageFileModel file = files.get(position);

        // Load image using Glide
        Glide.with(context)
                .load(file.getUrl())
                .placeholder(R.drawable.ic_default_profile)
                .error(R.drawable.ic_default_profile)
                .into(holder.fileImage);

        // Set file name
        holder.fileName.setText(file.getName());

        // Handle image click
        holder.fileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(file.getUrl()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public void updateFiles(List<StorageFileModel> newFiles) {
        files.clear();
        files.addAll(newFiles);
        notifyDataSetChanged();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        ImageView fileImage;
        TextView fileName;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileImage = itemView.findViewById(R.id.itemFileImage);
            fileName = itemView.findViewById(R.id.itemFileName);
        }
    }
}