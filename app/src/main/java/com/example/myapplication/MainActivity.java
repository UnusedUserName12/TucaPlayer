package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.db.DatabaseHelper;
import com.example.myapplication.db.DatabaseManager;
import com.example.myapplication.obj.Song;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPagerAdapter viewPagerAdapter;
    ThreadSeekBar threadSeekBar;
    ThreadElementAutoSelector threadElementAutoSelector;
    SeekBar seekBar;
    TextView currentTime;
    TextView totalTime;
    SongViewListeners songViewListeners;
    PlaylistView playlistView;
    UserSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager2 = findViewById(R.id.viewPager);
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(viewPagerAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Objects.requireNonNull(tabLayout.getTabAt(position)).select();
            }
        });

        seekBar = findViewById(R.id.seek_bar);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);


        songViewListeners = new SongViewListeners(this);
        songViewListeners.setListeners();

        playlistView = new PlaylistView(this);
        playlistView.setListeners();

        settings = new UserSettings();
        loadSharedPreferences();
        applySettingToSongView();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onStart() {
        super.onStart();

        if (!checkPermission()) {
                requestPermission();
        }

        updateSongTable();


        threadSeekBar = new ThreadSeekBar(seekBar,totalTime,currentTime);
        threadElementAutoSelector = new ThreadElementAutoSelector();

        threadSeekBar.start();
        threadElementAutoSelector.start();

        ImageView btnPlay = findViewById(R.id.btn_play_pause);
        if(settings.isSong_playing()) btnPlay.setImageResource(R.drawable.pause_24dp);

    }

    @Override
    protected void onStop() {
        super.onStop();
        ThreadElementAutoSelector.isStopped =true;
        ThreadSeekBar.isStopped =true;

        saveSongViewParameters();
    }



    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestPermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PackageManager.PERMISSION_GRANTED);
        }
        else{
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_MEDIA_AUDIO,Manifest.permission.READ_MEDIA_IMAGES},
                    PackageManager.PERMISSION_GRANTED);
        }

    }

    private void updateSongTable() {

        //Checking the folder
        List<String> folderFileList = new ArrayList<>();
        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloadsFolder.exists() && downloadsFolder.isDirectory()) {
            File[] files = downloadsFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".mp3")) {
                        folderFileList.add(file.getName());
                    }
                }

            }
        } else {
            Toast.makeText(this, "Downloads folder not found", Toast.LENGTH_SHORT).show();
        }

        //Checking the database
        DatabaseManager databaseManager = new DatabaseManager(this);
        try{
            databaseManager.open();
        }catch (Exception e){
            e.printStackTrace();
        }

        Cursor cursor = databaseManager.fetchSongs("title");
        List<String> dbFileList = new ArrayList<>();
        if(cursor.moveToFirst()){
            do {
                @SuppressLint("Range") String dbFILENAME = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_FILENAME));
                dbFileList.add(dbFILENAME);
                //Log.i("DATABASE_TAG", "I have read SONG : "+dbFILENAME);
            }while (cursor.moveToNext());
        }

        //Compare database and folder and insert if in folder but not db
        List<String> songsToAdd = new ArrayList<>(folderFileList);
        songsToAdd.removeAll(dbFileList);

        List<String> songsToDelete = new ArrayList<>(dbFileList);
        songsToDelete.removeAll(folderFileList);


        if(!songsToAdd.isEmpty()) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            for (String FolderFilename : songsToAdd) {
                File songFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FolderFilename);
                mmr.setDataSource(songFile.getPath());
                String song_name = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                if (song_name == null) song_name = songFile.getName().replace(".mp3", "");
                String song_album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                String song_artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String song_genre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
                long duration = Long.parseLong(Objects.requireNonNull(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));

                databaseManager.insertSong(FolderFilename, song_name, song_album, song_artist, song_genre,duration);
            }
        }


        //Delete from database if not in folder
        if(!songsToDelete.isEmpty()) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String ID = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ID));
                    @SuppressLint("Range") String dbFILENAME = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_FILENAME));

                    if(songsToDelete.contains(dbFILENAME)) databaseManager.deleteSong(Integer.parseInt(ID));
                } while (cursor.moveToNext());
            }
        }

        databaseManager.close();
        cursor.close();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (action == KeyEvent.ACTION_DOWN) {
                if (songViewListeners.isExpanded) {
                    songViewListeners.shrinkSong();
                } else if(PlaylistView.isExpanded) {
                    playlistView.shrinkPlaylist();
                }else{
                    this.finish();
                }
                return true;
            }
        }
        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == 3) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    playlistView.chosen_image.setImageURI(selectedImageUri);
                    playlistView.chosen_image.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public PlaylistView getPlaylistView() {
        return playlistView;
    }

    private void loadSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences(UserSettings.SONG_PREFERENCES,MODE_PRIVATE);
        int last_song_id = sharedPreferences.getInt(UserSettings.LAST_SONG_ID,-1);
        int last_playlist_id = sharedPreferences.getInt(UserSettings.LAST_PLAYLIST_ID,-1);
        boolean song_is_playing = sharedPreferences.getBoolean(UserSettings.SONG_IS_PLAYING,false);
        settings.setLast_song_id(last_song_id);
        settings.setLast_playlist_id(last_playlist_id);
        settings.setSong_playing(song_is_playing);
    }
    private void applySettingToSongView() {
        if(settings.getLast_song_id()<0){
            hideSongView();
        }
        else {
            TextView song_view_name = findViewById(R.id.song_view_name);
            DatabaseManager databaseManager = new DatabaseManager(this);
            try {
                databaseManager.open();
            } catch (SQLDataException e) {
                throw new RuntimeException(e);
            }

            Song song = databaseManager.fetchSongById(settings.getLast_song_id());
            song_view_name.setText(song.getSongName());
            List<Song> SongList = new ArrayList<>();
            Cursor cursor;
            if(settings.getLast_playlist_id()>0){
                cursor = databaseManager.fetchPlaylistSongsFullInfo(settings.getLast_playlist_id(),"title");
            }
            else {
                cursor = databaseManager.fetchSongs("title");
            }

            if(cursor.moveToFirst()){
                do {
                    @SuppressLint("Range") int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAY_SONGS_TABLE_SONG_ID)));
                    @SuppressLint("Range") String filename = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_FILENAME));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_NAME));
                    @SuppressLint("Range") String artist = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ARTIST));
                    @SuppressLint("Range") String album = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ALBUM));
                    @SuppressLint("Range") String genre = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_GENRE));
                    @SuppressLint("Range") long duration = Long.parseLong(cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_DURATION)));

                    Song songL = new Song(id, filename, name, artist, album, genre, duration);
                    SongList.add(songL);

                }while (cursor.moveToNext());
            }
            cursor.close();
            databaseManager.close();
            MyMediaPlayer.setSongList(SongList);
        }
    }

    public void showSongView(){
        LinearLayout bottom_panel = findViewById(R.id.empty_place);
        ImageView btnPlay = findViewById(R.id.btn_play_pause);
        ImageView btnNext = findViewById(R.id.btn_next);
        ImageView btnBack = findViewById(R.id.btn_back);
        CardView song_view_image_container = findViewById(R.id.song_view_image_container);
        TextView song_view_name = findViewById(R.id.song_view_name);

        bottom_panel.setVisibility(View.VISIBLE);
        btnPlay.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        song_view_image_container.setVisibility(View.VISIBLE);
        song_view_name.setVisibility(View.VISIBLE);
    }

    //TODO: fix SongView being visible if user opens a playlist without playing a song before (low priority)
    private void hideSongView(){
        LinearLayout bottom_panel = findViewById(R.id.empty_place);
        ImageView btnPlay = findViewById(R.id.btn_play_pause);
        ImageView btnNext = findViewById(R.id.btn_next);
        ImageView btnBack = findViewById(R.id.btn_back);
        CardView song_view_image_container = findViewById(R.id.song_view_image_container);
        TextView song_view_name = findViewById(R.id.song_view_name);

        bottom_panel.setVisibility(View.GONE);

        btnPlay.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        btnBack.setVisibility(View.GONE);
        song_view_image_container.setVisibility(View.GONE);
        song_view_name.setVisibility(View.GONE);
    }

    private void saveSongViewParameters() {
        settings.setLast_song_id(MyMediaPlayer.getCurrentSongId());
        MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
        settings.setSong_playing(mediaPlayer.isPlaying());
        //settings.setLast_playlist_id is located in PlaylistView and SongsTab

        SharedPreferences.Editor editor = getSharedPreferences(UserSettings.SONG_PREFERENCES,MODE_PRIVATE).edit();
        editor.putInt(UserSettings.LAST_SONG_ID,settings.getLast_song_id());
        editor.putInt(UserSettings.LAST_PLAYLIST_ID,settings.getLast_playlist_id());
        editor.putBoolean(UserSettings.SONG_IS_PLAYING,settings.isSong_playing());
        editor.apply();
    }

    private UserSettings getSettings(){
        return settings;
    }
}