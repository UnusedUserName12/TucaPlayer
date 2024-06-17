package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.content.Context;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
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
    private MP3ListAdapter AudioAdapter;
    ListView listView;
    private RelativeLayout top_panel;
    private RelativeLayout search_area;
    private SearchView searchView;
    private Button btn_add;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_songs_to_playlist);

        SongList = new ArrayList<>();
        SongsInPlaylistList = new ArrayList<>();

        getSongsFromPlaylist(SongsInPlaylistList);

        search_area = findViewById(R.id.search_bar_area);
        top_panel = findViewById(R.id.topPanel_add_songs);

        TextView textView = findViewById(R.id.choose_tracks);
        textView.setOnClickListener(v -> showSearchBar());

        ImageView btn_back = findViewById(R.id.btn_back_from_add_songs);
        btn_add = findViewById(R.id.btn_add_songs);
        btn_add.setEnabled(false);
        ImageView btn_sort = findViewById(R.id.btn_sort_add_songs);
        ImageView btn_select_all = findViewById(R.id.btn_select_all_add_songs);
        ImageView btn_search = findViewById(R.id.btn_search_add_songs);

        searchView = findViewById(R.id.search_bar_add_song);
        searchView.setFocusable(true);
        searchView.setFocusableInTouchMode(true);

        int searchIconId = searchView.getContext().getResources().getIdentifier("android:id/search_mag_icon", null, null);
        ImageView searchIcon = (ImageView) searchView.findViewById(searchIconId);

        searchIcon.setColorFilter(ContextCompat.getColor(AddSongsToPlaylistActivity.this, color.red), PorterDuff.Mode.SRC_IN);

        int closeIconId = searchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeIcon = (ImageView) searchView.findViewById(closeIconId);

        closeIcon.setColorFilter(ContextCompat.getColor(AddSongsToPlaylistActivity.this, color.red  ), PorterDuff.Mode.SRC_IN);
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
            Context wrapper = new ContextThemeWrapper(AddSongsToPlaylistActivity.this, R.style.CustomPopupMenu);
            PopupMenu popupMenu = new PopupMenu(wrapper, v);

            popupMenu.getMenuInflater().inflate(R.menu.sort_order_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                loadAudio((String) menuItem.getTitle()); //on click try to arrange a new list
                return true;
            });
            popupMenu.show();

        });

        btn_select_all.setOnClickListener(v -> {
            for(Song s : SongList){
                s.setSelected(!s.isSelected());
            }
            checkAddButton();

            AudioAdapter.notifyDataSetChanged();
        });

        AdapterView.OnItemClickListener itemListener = (parent, v, position, id) -> {
            List <Song> filteredList = AudioAdapter.getFilteredList();

            filteredList.get(position).setSelected(!SongList.get(position).isSelected());
            for(Song s : SongList){
                for(Song fs : filteredList){
                    if(s.getId()== fs.getId()){
                        s.setSelected(fs.isSelected());
                        break;
                    }
                }
            }

            checkAddButton();

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
                for (Song s : SongList) {
                    if(s.isSelected())
                        databaseManager.insertPlaylistSong(playlist_id,s.getId());
                }
            }
            else Toast.makeText(AddSongsToPlaylistActivity.this, "Something went wrong",Toast.LENGTH_SHORT).show();
            databaseManager.close();
            setResult(RESULT_OK);
            finish();
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
        AudioAdapter.notifyDataSetChanged();

        databaseManager.close();
        cursor.close();
    }


    private void showSearchBar(){
        top_panel.setVisibility(View.INVISIBLE);
        search_area.setVisibility(View.VISIBLE);
        searchView.setIconifiedByDefault(true);
        searchView.requestFocus();
        searchView.setIconified(false);
        searchView.requestFocusFromTouch();
    }

    private void getSongsFromPlaylist(List<Integer> list){
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

    private void checkAddButton(){
        boolean isEnabled = false;
        for (Song s : SongList){
            if (s.isSelected()) {
                isEnabled = true;
                break;
            }
        }

        if(isEnabled){
            btn_add.setEnabled(true);
            btn_add.setBackgroundTintList(ContextCompat.getColorStateList(AddSongsToPlaylistActivity.this, color.red));
        }
        else {
            btn_add.setEnabled(false);
            btn_add.setBackgroundTintList(ContextCompat.getColorStateList(AddSongsToPlaylistActivity.this, color.grey_66D6D6D6));
        }
    }
}
