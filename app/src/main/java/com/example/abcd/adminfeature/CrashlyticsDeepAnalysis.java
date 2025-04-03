package com.example.abcd.adminfeature;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.R;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CrashlyticsDeepAnalysis extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CrashLogAdapter adapter;
    private List<LogEntry> crashLogs = new ArrayList<>();
    private DatabaseReference logsRef;
    private LogStorageHelper logStorageHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crashlytics_deep_analysis);

        recyclerView = findViewById(R.id.recyclerViewCrashes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CrashLogAdapter(crashLogs);
        recyclerView.setAdapter(adapter);

        logsRef = FirebaseDatabase.getInstance().getReference("logs");
        logStorageHelper = new LogStorageHelper();

        fetchCrashLogs();
    }

    private void fetchCrashLogs() {
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        logStorageHelper.log("Crashlytics", "Admin is checking crash logs");  // Replaced Log

        logsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                crashLogs.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LogEntry log = snapshot.getValue(LogEntry.class);
                    if (log != null) {
                        crashLogs.add(log);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                logStorageHelper.log("Firebase", "Error fetching logs: " + databaseError.getMessage());
                FirebaseCrashlytics.getInstance().recordException(
                        new Exception("Firebase fetch error: " + databaseError.getMessage())
                );
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logsRef.removeEventListener((ValueEventListener) this);
    }
}