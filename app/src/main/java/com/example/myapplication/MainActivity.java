package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.interfaces.OnSongChangeListener;
import com.example.myapplication.db.DatabaseHelper;
import com.example.myapplication.db.DatabaseManager;
import com.example.myapplication.obj.Song;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    ViewPagerAdapter viewPagerAdapter;
    ConstraintSet initialSet;
    ThreadSeekBar threadSeekBar;
    ThreadElementAutoSelector threadElementAutoSelector;
    SeekBar seekBar;
    TextView currentTime;
    TextView totalTime;
    SongViewListeners songViewListeners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialSet = new ConstraintSet();

        tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.viewPager);
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

        TextView song_name_text_view = findViewById(R.id.song_name_play_song);
        ImageView btn_play = findViewById(R.id.btn_play_pause);
        ImageView btn_repeat = findViewById(R.id.btn_repeat);
        ImageView btn_shuffle = findViewById(R.id.btn_shuffle);
        ImageView btn_next = findViewById(R.id.btn_next);
        ImageView btn_back = findViewById(R.id.btn_back);
        ImageView btn_shrink = findViewById(R.id.btn_close_play_song);
        LinearLayout bottom_panel = findViewById(R.id.empty_place);
        TextView artist_view = findViewById(R.id.artist_play_song);

        seekBar = findViewById(R.id.seek_bar);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);

        ConstraintLayout constraintLayout = findViewById(R.id.main_layout);
        initialSet.clone(constraintLayout);
        ConstraintSet expandedSongSet = new ConstraintSet();
        expandedSongSet.clone(this, R.layout.song_view);


        songViewListeners = new SongViewListeners(this,initialSet,expandedSongSet, btn_play, song_name_text_view, artist_view);
        songViewListeners.setListeners(btn_play,btn_repeat,btn_next,btn_back,btn_shuffle,seekBar, bottom_panel,btn_shrink);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!checkPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermission();
            }
        }

        updateSongTable();

        threadSeekBar = new ThreadSeekBar(seekBar,totalTime,currentTime);
        threadElementAutoSelector = new ThreadElementAutoSelector();

        threadSeekBar.start();
        threadElementAutoSelector.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ThreadElementAutoSelector.isStopped =true;
        ThreadSeekBar.isStopped =true;
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this,
                new String[] {Manifest.permission.READ_MEDIA_AUDIO},
                PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this,
                new String[] {Manifest.permission.READ_MEDIA_IMAGES},
                PackageManager.PERMISSION_GRANTED);
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
                } else {
                    this.finish();
                }
                return true;
            }
        }
        return false;
    }

}