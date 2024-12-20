package com.example.abcd.videoplayers1;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.button.MaterialButton;
import com.example.abcd.R;

public class VideoShowActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    private MaterialButton speedButton;
    private ImageButton fullscreenButton;
    private TextView titleView;
    private ConstraintLayout controlsLayout;
    private boolean isFullscreen = false;
    private float[] playbackSpeeds = {0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f};
    private int currentSpeedIndex = 2; // Default 1.0x

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_show);

        initializeViews();
        String videoUrl = getIntent().getStringExtra("videoUrl");
        String videoTitle = getIntent().getStringExtra("videoTitle");

        initializePlayer(videoUrl);
        setupSpeedControl();
        setupFullscreenButton();
        
        titleView.setText(videoTitle);
    }

    private void initializeViews() {
        playerView = findViewById(R.id.player_view);
        speedButton = findViewById(R.id.speed_button);
        fullscreenButton = findViewById(R.id.exo_fullscreen_button);
        titleView = findViewById(R.id.video_title);
        controlsLayout = findViewById(R.id.controls_layout);
    }

    private void initializePlayer(String videoUrl) {
        try {
            player = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(player);

            player.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(com.google.android.exoplayer2.PlaybackException error) {
                    Toast.makeText(VideoShowActivity.this, 
                        "Error playing video: " + error.getMessage(), 
                        Toast.LENGTH_LONG).show();
                }

                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_READY) {
                        findViewById(R.id.loading_indicator).setVisibility(View.GONE);
                    } else if (state == Player.STATE_BUFFERING) {
                        findViewById(R.id.loading_indicator).setVisibility(View.VISIBLE);
                    }
                }
            });

            MediaItem mediaItem = MediaItem.fromUri(videoUrl);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing player: " + e.getMessage(), 
                Toast.LENGTH_LONG).show();
        }
    }

    private void setupSpeedControl() {
        speedButton.setText(String.format("%.2fx", playbackSpeeds[currentSpeedIndex]));
        speedButton.setOnClickListener(v -> {
            currentSpeedIndex = (currentSpeedIndex + 1) % playbackSpeeds.length;
            float newSpeed = playbackSpeeds[currentSpeedIndex];
            player.setPlaybackParameters(new PlaybackParameters(newSpeed));
            speedButton.setText(String.format("%.2fx", newSpeed));
            Toast.makeText(this, "Playback speed: " + String.format("%.2fx", newSpeed), 
                Toast.LENGTH_SHORT).show();
        });
    }

    private void setupFullscreenButton() {
        fullscreenButton.setOnClickListener(v -> toggleFullscreen());
    }

    private void toggleFullscreen() {
        if (isFullscreen) {
            // Exit fullscreen
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) playerView.getLayoutParams();
            params.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
            params.height = getResources().getDimensionPixelSize(R.dimen.player_portrait_height);
            playerView.setLayoutParams(params);
            controlsLayout.setVisibility(View.VISIBLE);
            fullscreenButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_enter));
        } else {
            // Enter fullscreen
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) playerView.getLayoutParams();
            params.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
            params.height = ConstraintLayout.LayoutParams.MATCH_PARENT;
            playerView.setLayoutParams(params);
            controlsLayout.setVisibility(View.GONE);
            fullscreenButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_exit));
        }
        isFullscreen = !isFullscreen;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (!isFullscreen) toggleFullscreen();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (isFullscreen) toggleFullscreen();
        }
    }

    @Override
    public void onBackPressed() {
        if (isFullscreen) {
            toggleFullscreen();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}