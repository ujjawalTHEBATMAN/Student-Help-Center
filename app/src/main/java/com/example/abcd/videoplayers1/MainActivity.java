package com.example.abcd.videoplayers1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.abcd.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainvideoplay);

        Button sem1Button = findViewById(R.id.sem1_button);
        Button sem2Button = findViewById(R.id.sem2_button);
        Button sem3Button = findViewById(R.id.sem3_button);
        Button sem4Button = findViewById(R.id.sem4_button);
        Button sem5Button = findViewById(R.id.sem5_button);
        Button sem6Button = findViewById(R.id.sem6_button);

        sem1Button.setOnClickListener(v -> navigateToPlaylist("sem1"));
        sem2Button.setOnClickListener(v -> navigateToPlaylist("sem2"));
        sem3Button.setOnClickListener(v -> navigateToPlaylist("sem3"));
        sem4Button.setOnClickListener(v -> navigateToPlaylist("sem4"));
        sem5Button.setOnClickListener(v -> navigateToPlaylist("sem5"));
        sem6Button.setOnClickListener(v -> navigateToPlaylist("sem6"));
    }

private void navigateToPlaylist(String semester) {
    Intent intent = new Intent(this, PlaylistShowActivity.class);
    intent.putExtra("semester", semester);
    startActivity(intent);
}
}