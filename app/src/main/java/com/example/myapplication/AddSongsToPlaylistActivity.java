package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R.color;
import com.example.myapplication.db.DatabaseHelper;
import com.example.myapplication.db.DatabaseManager;
import com.example.myapplication.obj.Song;

import java.util.ArrayList;
import java.util.List;

public class AddSongsToPlaylistActivity extends Activity {

    private List<Song> SongList;
    private List<Integer> SongsInPlaylistList;
    private List<Integer> SelectedSongList;
    private MP3ListAdapter AudioAdapter;
    ListView listView;
    private RelativeLayout top_panel;
    private RelativeLayout search_area;
    private SearchView searchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_songs_to_playlist);

        SongList = new ArrayList<>();
        SelectedSongList = new ArrayList<>();
        SongsInPlaylistList = new ArrayList<>();

        getSongsInPlaylist(SongsInPlaylistList);

        search_area = findViewById(R.id.search_bar_area);
        top_panel = findViewById(R.id.topPanel_add_songs);

        TextView textView = findViewById(R.id.choose_tracks);
        textView.setOnClickListener(v -> showSearchBar());

        ImageView btn_back = findViewById(R.id.btn_back_from_add_songs);
        Button btn_add = findViewById(R.id.btn_add_songs);
        btn_add.setEnabled(false);
        ImageView btn_sort = findViewById(R.id.btn_sort_add_songs);
        ImageView btn_select_all = findViewById(R.id.btn_select_all_add_songs);
        ImageView btn_search = findViewById(R.id.btn_search_add_songs);

        searchView = findViewById(R.id.search_bar_add_song);
        searchView.setFocusable(true);
        searchView.setFocusableInTouchMode(true);
        listView = findViewById(R.id.add_songs_list);




        AudioAdapter = new MP3ListAdapter(this, SongList);
        listView.setAdapter(AudioAdapter);
        loadAudio("title");

        //listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        btn_back.setOnClickListener(v -> this.finish());

        btn_search.setOnClickListener(v -> showSearchBar());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search_area.setVisibility(View.INVISIBLE);
                top_panel.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                AudioAdapter.getFilter().filter(newText);
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            search_area.setVisibility(View.INVISIBLE);
            top_panel.setVisibility(View.VISIBLE);
            return false;
        });

        btn_sort.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(AddSongsToPlaylistActivity.this, v);

            popupMenu.getMenuInflater().inflate(R.menu.sort_order_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                loadAudio((String) menuItem.getTitle()); //on click try to arrange a new list
                updateSelection();

                return true;
            });
            popupMenu.show();

        });

        btn_select_all.setOnClickListener(v -> {
            for(Song s : SongList){
                if (!SelectedSongList.contains(s.getId()))
                    SelectedSongList.add(s.getId());
                else SelectedSongList.remove(SelectedSongList.indexOf(s.getId()));
            }

            updateSelection();

            if(SelectedSongList.isEmpty()){
                btn_add.setEnabled(false);
                btn_add.setBackgroundTintList(ContextCompat.getColorStateList(AddSongsToPlaylistActivity.this, color.grey_66D6D6D6));
            }
            else {
                btn_add.setEnabled(true);
                btn_add.setBackgroundTintList(ContextCompat.getColorStateList(AddSongsToPlaylistActivity.this, color.red));
            }

            AudioAdapter.notifyDataSetChanged();
        });

        AdapterView.OnItemClickListener itemListener = (parent, v, position, id) -> {
            List <Song> filteredList = AudioAdapter.getFilteredList();
            Song clickedSong = filteredList.get(position);
            //Song clickedSong = (Song) parent.getItemAtPosition(position);

            if (!SelectedSongList.contains(clickedSong.getId()))
                SelectedSongList.add(clickedSong.getId());
            else SelectedSongList.remove(SelectedSongList.indexOf(clickedSong.getId()));

            updateSelection();

            if(SelectedSongList.isEmpty()){
                btn_add.setEnabled(false);
                btn_add.setBackgroundTintList(ContextCompat.getColorStateList(AddSongsToPlaylistActivity.this, color.grey_66D6D6D6));
            }
            else {
                btn_add.setEnabled(true);
                btn_add.setBackgroundTintList(ContextCompat.getColorStateList(AddSongsToPlaylistActivity.this, color.red));
            }

            AudioAdapter.notifyDataSetChanged();
        };
        listView.setOnItemClickListener(itemListener);

        btn_add.setOnClickListener(v -> {

            int playlist_id = getIntent().getIntExtra("playlist_id",0);
            DatabaseManager databaseManager = new DatabaseManager(this);
            try{
                databaseManager.open();
            }catch (Exception e){
                e.printStackTrace();
            }
            if(playlist_id !=0) {
                for (int song_id : SelectedSongList) {
                    databaseManager.insertPlaylistSong(playlist_id,song_id);
                }
            }
            else Toast.makeText(AddSongsToPlaylistActivity.this, "Something went wrong",Toast.LENGTH_SHORT).show();
            databaseManager.close();
            AddSongsToPlaylistActivity.this.finish();
        });
    }



    private void loadAudio(String orderOption) {
        SongList.clear();

        DatabaseManager databaseManager = new DatabaseManager(this);
        try{
            databaseManager.open();
        }catch (Exception e){
            e.printStackTrace();
        }

        Cursor cursor = databaseManager.fetchSongs(orderOption);
        if(cursor.moveToFirst()){
            do {
                @SuppressLint("Range") int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ID)));
                @SuppressLint("Range") String filename = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_FILENAME));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_NAME));
                @SuppressLint("Range") String artist = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ARTIST));
                @SuppressLint("Range") String album = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ALBUM));
                @SuppressLint("Range") String genre = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_GENRE));
                @SuppressLint("Range") long duration = Long.parseLong(cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_DURATION)));

                if(!SongsInPlaylistList.contains(id)) {
                    Song song = new Song(id, filename, name, artist, album, genre, duration);
                    SongList.add(song);
                }

            }while (cursor.moveToNext());
        }

        databaseManager.close();
        cursor.close();
    }

    private void updateSelection(){
        for (Song s : SongList) {
            s.setSelected(false);
        }
        for (Song s : SongList) {
            for(int i : SelectedSongList)     //Contains looks for "PERFECT" match
                if(i==s.getId()) SongList.get(SongList.indexOf(s)).setSelected(true);
            }
        AudioAdapter.notifyDataSetChanged();
    }

    private void showSearchBar(){
        top_panel.setVisibility(View.INVISIBLE);
        search_area.setVisibility(View.VISIBLE);
        searchView.setIconifiedByDefault(true);
        searchView.requestFocus();
        searchView.setIconified(false);
        searchView.requestFocusFromTouch();
    }

    private void getSongsInPlaylist(List<Integer> list){
        DatabaseManager databaseManager = new DatabaseManager(this);
        try{
            databaseManager.open();
        }catch (Exception e){
            e.printStackTrace();
        }

        int playlist_id = getIntent().getIntExtra("playlist_id",0);

        Cursor cursor = databaseManager.fetchPlaylistSongs(playlist_id);
        if(cursor.moveToFirst()){
            do {
                @SuppressLint("Range") int song_id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAY_SONGS_TABLE_SONG_ID)));
                list.add(song_id);
            }while (cursor.moveToNext());
        }
        databaseManager.close();
    }
}
