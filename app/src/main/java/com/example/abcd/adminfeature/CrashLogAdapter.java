package com.example.abcd.adminfeature;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.R;

import java.util.List;

public class CrashLogAdapter extends RecyclerView.Adapter<CrashLogAdapter.ViewHolder> {

    private final List<LogEntry> crashLogs;

    public CrashLogAdapter(List<LogEntry> crashLogs) {
        this.crashLogs = crashLogs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crash_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LogEntry log = crashLogs.get(position);
        holder.logTitle.setText(log.logTitle);
        holder.logMessage.setText(log.logMessage);
        holder.timestamp.setText(log.timestamp);
        holder.email.setText("Reported by: " + log.email);
    }

    @Override
    public int getItemCount() {
        return crashLogs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView logTitle, logMessage, timestamp, email;

        ViewHolder(View itemView) {
            super(itemView);
            logTitle = itemView.findViewById(R.id.tvLogTitle);
            logMessage = itemView.findViewById(R.id.tvLogMessage);
            timestamp = itemView.findViewById(R.id.tvTimestamp);
            email = itemView.findViewById(R.id.tvEmail);
        }
    }
}
