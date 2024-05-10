package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.myapplication.db.DatabaseHelper;
import com.example.myapplication.db.DatabaseManager;
import com.example.myapplication.obj.Song;

import java.util.ArrayList;
import java.util.List;

public class AddSongsToPlaylistActivity extends Activity {

    private List<Song> SongList;
    private List<Song> FilteredList;
    private MP3ListAdapter AudioAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_songs_to_playlist);

        RelativeLayout search_area = findViewById(R.id.search_bar_area);
        RelativeLayout top_panel = findViewById(R.id.topPanel_add_songs);

        TextView textView = findViewById(R.id.choose_tracks);

        ImageView btn_back = findViewById(R.id.btn_back_from_add_songs);
        Button btn_add = findViewById(R.id.btn_add_songs);
        ImageView btn_sort = findViewById(R.id.btn_sort_add_songs);
        ImageView btn_select_all = findViewById(R.id.btn_select_all_add_songs);
        ImageView btn_search = findViewById(R.id.btn_search_add_songs);

        SearchView searchView = findViewById(R.id.search_bar_add_song);
        searchView.setFocusable(true);
        searchView.setFocusableInTouchMode(true);
        ListView listView = findViewById(R.id.add_songs_list);

        SongList = new ArrayList<>();

        AudioAdapter = new MP3ListAdapter(this, SongList);
        loadAudio();
        listView.setAdapter(AudioAdapter);

        btn_back.setOnClickListener(v -> {
            this.finish();
        });

        btn_search.setOnClickListener(v -> {
            top_panel.setVisibility(View.INVISIBLE);
            search_area.setVisibility(View.VISIBLE);
            searchView.setIconifiedByDefault(true);
            searchView.requestFocus();
            searchView.setIconified(false);
            searchView.requestFocusFromTouch();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search_area.setVisibility(View.INVISIBLE);
                top_panel.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                filterList(newText);
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            search_area.setVisibility(View.INVISIBLE);
            top_panel.setVisibility(View.VISIBLE);
            return false;
        });

    }

    private void loadAudio() {
        DatabaseManager databaseManager = new DatabaseManager(this);
        try{
            databaseManager.open();
        }catch (Exception e){
            e.printStackTrace();
        }

        Cursor cursor = databaseManager.fetchSongs();
        if(cursor.moveToFirst()){
            do {
                @SuppressLint("Range") String filename = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_FILENAME));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_NAME));
                @SuppressLint("Range") String artist = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ARTIST));
                @SuppressLint("Range") String album = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ALBUM));
                @SuppressLint("Range") String genre = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_GENRE));

                Song song = new Song(filename,name,artist,album);
                SongList.add(song);

            }while (cursor.moveToNext());
        }
        AudioAdapter.notifyDataSetChanged();

        databaseManager.close();
        cursor.close();
    }

    //TODO FIX ADAPTER

    private void filterList(String text){
        AudioAdapter.setSongList(SongList);
        List<Song> filteredList = new ArrayList<>();
        for(Song song : SongList){
            if(song.getSongName().toLowerCase().contains(text.toLowerCase()))
                filteredList.add(song);
        }

        if(!filteredList.isEmpty()) {
            AudioAdapter.setSongList(filteredList);

        }
        AudioAdapter.notifyDataSetChanged();
    }
}
