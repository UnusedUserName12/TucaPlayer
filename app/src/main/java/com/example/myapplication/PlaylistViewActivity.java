package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myapplication.db.DatabaseHelper;
import com.example.myapplication.db.DatabaseManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLDataException;


public class PlaylistViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_view);

        TextView playlist_name_view = findViewById(R.id.playlist_name);
        ImageView playlist_image_view = findViewById(R.id.playlist_image);
        ListView playlist_songs_view = findViewById(R.id.playlist_songs);
        ImageButton btn_add_songs = findViewById(R.id.btn_add_song_redirect);
        ImageButton btn_more = findViewById(R.id.btn_more_playlist);

        DatabaseManager databaseManager = new DatabaseManager(this);
        try {
            databaseManager.open();
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
        int id = getIntent().getIntExtra("card_playlist_id",0);
        Cursor cursor = databaseManager.getPlaylistById(id);

        if(cursor.moveToFirst()){
            @SuppressLint("Range") String ID = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAYLIST_ID));
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAYLIST_NAME));
            @SuppressLint("Range") String image_path = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAYLIST_PICTURE));

            playlist_name_view.setText(name);
            if(!image_path.equals("placeholder.png"))
            {
                playlist_image_view.setImageBitmap(loadImageFromStorage(image_path,name));
                playlist_image_view.setClipToOutline(true);
            }
        }
        databaseManager.close();
        cursor.close();

        btn_add_songs.setOnClickListener(this::openAddSongsActivity);
    }

    private void openAddSongsActivity(View v){
        Context context = getBaseContext();
        int id = getIntent().getIntExtra("card_playlist_id",0);
        Intent intent = new Intent(context, AddSongsToPlaylistActivity.class);
        intent.putExtra("playlist_id",id);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private Bitmap loadImageFromStorage(String path, String playlistName) {
        try {
            File f = new File(path, playlistName+".jpg");
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
