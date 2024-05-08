package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class PlaylistViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_view);

        TextView playlist_name_view = findViewById(R.id.playlist_name);
        ImageView playlist_image_view = findViewById(R.id.playlist_image);
        ListView playlist_songs_view = findViewById(R.id.playlist_songs);
        ImageButton btn_add_songs = findViewById(R.id.btn_add_song_to_playlist);
        ImageButton btn_more = findViewById(R.id.btn_more_playlist);
    }
}
