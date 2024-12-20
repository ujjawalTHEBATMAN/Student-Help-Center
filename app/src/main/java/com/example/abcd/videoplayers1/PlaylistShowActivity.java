package com.example.abcd.videoplayers1;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.abcd.R;
import com.google.android.material.snackbar.Snackbar;

public class PlaylistShowActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private VideoViewModel videoViewModel;
    private ProgressBar progressBar;
    private TextView emptyView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_show);

        initializeViews();
        setupRecyclerView();
        setupViewModel();
        setupSwipeRefresh();
        loadVideos();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        emptyView = findViewById(R.id.empty_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
    }

    private void setupViewModel() {
        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);
        
        // Observe loading state
        videoViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                emptyView.setVisibility(View.GONE);
            }
        });

        // Observe error state
        videoViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
        });

        // Observe video list
        videoViewModel.getVideoList().observe(this, videos -> {
            if (videos != null) {
                if (videos.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    videoAdapter = new VideoAdapter(videos);
                    recyclerView.setAdapter(videoAdapter);
                }
            }
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadVideos);
        swipeRefreshLayout.setColorSchemeResources(
            R.color.gradient_start,
            R.color.gradient_end,
            R.color.gray
        );
    }

    private void loadVideos() {
        String semester = getIntent().getStringExtra("semester");
        if (semester != null) {
            videoViewModel.fetchVideoData(semester);
        } else {
            showError("No semester selected");
            finish();
        }
    }

    private void showError(String message) {
        Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG)
            .setAction("Retry", v -> loadVideos())
            .show();
    }
}
