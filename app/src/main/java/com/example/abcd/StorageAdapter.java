package com.example.abcd;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class StorageAdapter extends RecyclerView.Adapter<StorageAdapter.FileViewHolder> {
    private List<StorageFileModel> files;
    private Context context;
    private DeleteListener deleteListener;

    public interface DeleteListener {
        void onDeleteFile(String fileId);
    }

    public StorageAdapter(Context context, DeleteListener deleteListener) {
        this.context = context;
        this.deleteListener = deleteListener;
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

        holder.fileName.setText(file.getName());

        if (!file.getUrl().isEmpty()) {
            Glide.with(context)
                    .load(file.getUrl())
                    .placeholder(R.drawable.ic_chat)
                    .error(R.drawable.ic_chat)
                    .into(holder.fileImage);
        } else {
            holder.fileImage.setImageResource(R.drawable.ic_chat);
        }

        holder.fileImage.setOnClickListener(v -> {
            String url = file.getUrl();
            if (url.isEmpty()) return;

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show();
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteFile(file.getId());
            }
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
        ImageView deleteButton;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileImage = itemView.findViewById(R.id.itemFileImage);
            fileName = itemView.findViewById(R.id.itemFileName);
            deleteButton = itemView.findViewById(R.id.itemDeleteButton);
        }
    }
}