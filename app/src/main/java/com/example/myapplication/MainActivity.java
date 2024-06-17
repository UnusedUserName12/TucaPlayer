package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.db.DatabaseHelper;
import com.example.myapplication.db.DatabaseManager;
import com.example.myapplication.obj.Song;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    SongView songView;
    PlaylistView playlistView;
    UserSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_song_view_hidden);

        tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager2 = findViewById(R.id.viewPager);
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(viewPagerAdapter);
        viewPager2.setCurrentItem(0,false);


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


        songView = new SongView(this);
        songView.setListeners();

        playlistView = new PlaylistView(this);
        playlistView.setListeners();

        settings = new UserSettings();


    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onStart() {
        super.onStart();

        if (!checkPermission()) {
                requestPermission();
        }

        updateSongTable();
        loadSharedPreferences();
        applySettingToSongView();


        threadSeekBar = new ThreadSeekBar(seekBar,totalTime,currentTime);
        threadElementAutoSelector = new ThreadElementAutoSelector();

        threadSeekBar.start();
        threadElementAutoSelector.start();

        ViewPager2 viewPager2 = findViewById(R.id.viewPager);
        viewPager2.setCurrentItem(1,false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ThreadElementAutoSelector.isStopped =true;
        ThreadSeekBar.isStopped =true;

        saveSongViewParameters();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

        DatabaseManager databaseManager = new DatabaseManager(this);
        try{
            databaseManager.open();
            List<String> folderFileList = fetchSongsFromFolder();
            List<String> dbFileList = fetchSongsFromDB(databaseManager);

            List<String> songsToAdd = new ArrayList<>(folderFileList);
            songsToAdd.removeAll(dbFileList);

            List<String> songsToDelete = new ArrayList<>(dbFileList);
            songsToDelete.removeAll(folderFileList);

            addSongsToDB(songsToAdd,databaseManager);

            deleteSongsFromDB(songsToDelete,databaseManager);

        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            databaseManager.close();
        }
    }

    private List<String> fetchSongsFromFolder(){
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
        return folderFileList;
    }

    private List<String> fetchSongsFromDB(DatabaseManager databaseManager){
        Cursor cursor = databaseManager.fetchSongs("title");
        List<String> dbFileList = new ArrayList<>();
        if(cursor.moveToFirst()){
            do {
                @SuppressLint("Range") String dbFILENAME = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_FILENAME));
                dbFileList.add(dbFILENAME);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return dbFileList;
    }

    private void addSongsToDB (List<String> songsToAdd, DatabaseManager databaseManager){
        if(!songsToAdd.isEmpty()) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            for (String FolderFilename : songsToAdd) {
                File songFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FolderFilename);
                mmr.setDataSource(songFile.getPath());
                String song_name = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                if (song_name == null) song_name = songFile.getName().replace(".mp3", "");
                String song_album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                if (song_album == null) song_album = "Unknown";
                String song_artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                if (song_artist == null) song_artist = "Unknown";
                String song_genre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
                if (song_genre == null) song_genre = "Unknown";
                long duration = Long.parseLong(Objects.requireNonNull(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));

                databaseManager.insertSong(FolderFilename, song_name, song_album, song_artist, song_genre,duration);
            }
        }
    }

    private void deleteSongsFromDB(List<String> songsToDelete, DatabaseManager databaseManager){
        if(!songsToDelete.isEmpty()) {
            Cursor cursor = databaseManager.fetchSongs("title");
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String ID = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ID));
                    @SuppressLint("Range") String dbFILENAME = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_FILENAME));

                    if(songsToDelete.contains(dbFILENAME)) databaseManager.deleteSong(Integer.parseInt(ID));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (action == KeyEvent.ACTION_DOWN) {
                if (SongView.isExpanded) {
                    songView.shrinkSong();
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
        boolean is_song_on_repeat = sharedPreferences.getBoolean(UserSettings.SONG_IS_ON_REPEAT,false);
        boolean is_song_on_shuffle = sharedPreferences.getBoolean(UserSettings.SONG_IS_ON_SHUFFLE,false);
        boolean is_album = sharedPreferences.getBoolean(UserSettings.IS_ALBUM,false);
        settings.setLast_song_id(last_song_id);
        settings.setLast_playlist_id(last_playlist_id);
        settings.setSong_playing(song_is_playing);
        settings.setSong_is_on_repeat(is_song_on_repeat);
        settings.setSong_is_on_shuffle(is_song_on_shuffle);
        settings.setIsAlbum(is_album);
    }
    private void applySettingToSongView() {
        if(settings.getLast_song_id()>0){
            int last_song_id = settings.getLast_song_id();
            int last_playlist_id = settings.getLast_playlist_id();
            boolean is_album = settings.isAlbum();
            songView.showSong();
            TextView song_view_name = findViewById(R.id.song_view_name);
            DatabaseManager databaseManager = new DatabaseManager(this);
            try {
                databaseManager.open();
            } catch (SQLDataException e) {
                throw new RuntimeException(e);
            }

            Song song = databaseManager.fetchSongById(last_song_id);
            song_view_name.setText(song.getSongName());
            song_view_name.setSelected(true);
            List<Song> SongList = new ArrayList<>();

            Cursor song_cursor;
            Cursor playlist_cursor = null;

            if(last_playlist_id>0 && !is_album){
                //load songs from a playlist
                song_cursor = databaseManager.fetchPlaylistSongsFullInfo(last_playlist_id,"title");
                playlist_cursor = databaseManager.getPlaylistById(last_playlist_id);
            }
            else if(last_playlist_id>0 && is_album) {
                song_cursor = databaseManager.fetchAlbumsSongsFullInfo(last_playlist_id,"title");
                playlist_cursor = databaseManager.fetchAlbumById(last_playlist_id);
            }else {
                //load all songs
                song_cursor = databaseManager.fetchSongs("title");
            }

            if(song_cursor.moveToFirst()){
                do {
                    @SuppressLint("Range") int id = Integer.parseInt(song_cursor.getString(song_cursor.getColumnIndex(DatabaseHelper.SONG_ID)));
                    @SuppressLint("Range") String filename = song_cursor.getString(song_cursor.getColumnIndex(DatabaseHelper.SONG_FILENAME));
                    @SuppressLint("Range") String name = song_cursor.getString(song_cursor.getColumnIndex(DatabaseHelper.SONG_NAME));
                    @SuppressLint("Range") String artist = song_cursor.getString(song_cursor.getColumnIndex(DatabaseHelper.SONG_ARTIST));
                    @SuppressLint("Range") String album = song_cursor.getString(song_cursor.getColumnIndex(DatabaseHelper.SONG_ALBUM));
                    @SuppressLint("Range") String genre = song_cursor.getString(song_cursor.getColumnIndex(DatabaseHelper.SONG_GENRE));
                    @SuppressLint("Range") long duration = Long.parseLong(song_cursor.getString(song_cursor.getColumnIndex(DatabaseHelper.SONG_DURATION)));

                    SongList.add(new Song(id, filename, name, artist, album, genre, duration));

                }while (song_cursor.moveToNext());
            }
            song_cursor.close();
            ImageView song_view_image = findViewById(R.id.song_view_image);
            //Load playlist image if user exited with saved playlist
            if(playlist_cursor!=null && !is_album && playlist_cursor.moveToFirst()) {
                @SuppressLint("Range") String playlist_name = playlist_cursor.getString(playlist_cursor.getColumnIndex(DatabaseHelper.PLAYLIST_NAME));
                song_view_image.setImageBitmap(loadImageFromStorage(playlist_name));
                playlist_cursor.close();
            }
            if(playlist_cursor!=null && is_album && playlist_cursor.moveToFirst()){
                @SuppressLint("Range") String album_name = playlist_cursor.getString(playlist_cursor.getColumnIndex(DatabaseHelper.ALBUM_NAME));
                song_view_image.setImageBitmap(loadImageFromStorage(album_name));
                playlist_cursor.close();
            }

            databaseManager.close();

            MyMediaPlayer.setSongList(SongList);
            for(Song s : SongList)
                if(s.getId()==song.getId()) {
                    s.setSelected(true);
                    MyMediaPlayer.CurrentIndex = SongList.indexOf(s);
                }
            ThreadElementAutoSelector.SongList = SongList;
        }

        ImageView btnPlay = findViewById(R.id.btn_play_pause);
        if(settings.isSong_playing()) btnPlay.setImageResource(R.drawable.pause_24dp);
        ImageView btnRepeat = findViewById(R.id.btn_repeat);
        ImageView btnShuffle = findViewById(R.id.btn_shuffle);
        if(settings.isSong_on_repeat()) {
            MyMediaPlayer.onRepeat = true;
            btnRepeat.setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN);
        }
        if(settings.isSong_on_shuffle()){
            MyMediaPlayer.onShuffle = true;
            btnShuffle.setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN);
        }
    }

    private void saveSongViewParameters() {
        //settings.setLast_song_id is located in SongView
        MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
        settings.setSong_playing(mediaPlayer.isPlaying());
        //settings.setLast_playlist_id is located in PlaylistView and SongsTab

        SharedPreferences.Editor editor = getSharedPreferences(UserSettings.SONG_PREFERENCES,MODE_PRIVATE).edit();
        editor.putInt(UserSettings.LAST_SONG_ID,settings.getLast_song_id());
        editor.putInt(UserSettings.LAST_PLAYLIST_ID,settings.getLast_playlist_id());
        editor.putBoolean(UserSettings.SONG_IS_PLAYING,settings.isSong_playing());
        editor.putBoolean(UserSettings.SONG_IS_ON_REPEAT,settings.isSong_on_repeat());
        editor.putBoolean(UserSettings.SONG_IS_ON_SHUFFLE,settings.isSong_on_shuffle());
        editor.putBoolean(UserSettings.IS_ALBUM,settings.isAlbum());
        editor.apply();
    }

    public Bitmap loadImageFromStorage(String playlistName) {
        ContextWrapper cw = new ContextWrapper(this);
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        try {
            File f = new File(directory, playlistName+".jpg");
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String saveToInternalStorage(Bitmap bitmapImage,String playlistName){
        ContextWrapper cw = new ContextWrapper(this);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,playlistName+".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private UserSettings getSettings(){
        return settings;
    }
}