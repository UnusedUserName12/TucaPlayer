package com.example.myapplication;

import static com.example.myapplication.MyMediaPlayer.onRepeat;
import static com.example.myapplication.MyMediaPlayer.onShuffle;
import static com.example.myapplication.MyMediaPlayer.playMedia;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.KeyEvent;
import android.widget.ImageView;
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
import java.util.Random;

public class MainActivity extends AppCompatActivity implements OnSongChangeListener {

    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    ViewPagerAdapter viewPagerAdapter;
    boolean isShown = false;
    boolean isExpanded = false;
    static MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    private ImageView btn_play;
    private ImageView btn_repeat;
    private ConstraintSet initialSet;
    ThreadSeekBar threadSeekBar;
    ThreadElementAutoSelector threadElementAutoSelector;
    TextView btn_expand_play_song;
    TextView artist_view;

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

        btn_expand_play_song = findViewById(R.id.song_name_play_song);
        btn_expand_play_song.setOnClickListener(v->{
            if(!isExpanded) {
                expandSong();
            }
        });

        ImageView btn_shrink = findViewById(R.id.btn_close_play_song);
        btn_shrink.setOnClickListener(v -> {
            if(isExpanded) {
                shrinkSong();
            }
        });

        btn_play = findViewById(R.id.btn_play_pause);
        btn_play.setOnClickListener(v -> {
            if(!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                btn_play.setImageResource(R.drawable.pause_24dp);
            }
            else{
                mediaPlayer.pause();
                btn_play.setImageResource(R.drawable.play_arrow_24dp);
            }
        });

        ImageView btn_next = findViewById(R.id.btn_next);
        btn_next.setOnClickListener(v ->{
            if(MyMediaPlayer.getSongList() != null) {
                if (MyMediaPlayer.CurrentIndex == MyMediaPlayer.getSongListSize() - 1) {
                    MyMediaPlayer.CurrentIndex = -1;
                } else if (onShuffle) {
                    Random ran = new Random(System.currentTimeMillis());
                    MyMediaPlayer.CurrentIndex = ran.nextInt(MyMediaPlayer.getSongListSize());
                } else {
                    MyMediaPlayer.CurrentIndex = MyMediaPlayer.CurrentIndex + 1;
                }
                playMedia(MyMediaPlayer.CurrentIndex);
            }
        });

        ImageView btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(v -> {
            if(MyMediaPlayer.getSongList() != null) {
                if (MyMediaPlayer.CurrentIndex == 0) {
                    MyMediaPlayer.CurrentIndex = MyMediaPlayer.getSongListSize();
                }
                if (MyMediaPlayer.CurrentIndex != -1) {
                    --MyMediaPlayer.CurrentIndex;
                }
                playMedia(MyMediaPlayer.CurrentIndex);
            }
        });

        //TODO: change how background is set
        btn_repeat = findViewById(R.id.btn_repeat);
        btn_repeat.setOnClickListener(v -> {
            if(!onRepeat){
                onRepeat=true;
                btn_repeat.setBackground(new ColorDrawable(Color.rgb(240, 240, 240)));
            }
            else {
                onRepeat=false;
                btn_repeat.setBackground(new ColorDrawable(Color.WHITE));
            }
        });

        //TODO: change how background is set
        ImageView btn_shuffle = findViewById(R.id.btn_shuffle);
        btn_shuffle.setOnClickListener(v -> {
            if(!onShuffle){
                onShuffle=true;
                btn_shuffle.setBackground(new ColorDrawable(Color.rgb(240, 240, 240)));
            }
            else {
                onShuffle=false;
                btn_shuffle.setBackground(new ColorDrawable(Color.WHITE));
            }
        });


        SeekBar seekBar = findViewById(R.id.seek_bar);
        TextView currentTime = findViewById(R.id.currentTime);
        TextView totalTime = findViewById(R.id.totalTime);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer!=null && fromUser){
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        artist_view = findViewById(R.id.artist_play_song);

        MyMediaPlayer.setOnSongChangeListener(this);

        threadSeekBar = new ThreadSeekBar(seekBar,totalTime,currentTime);
        threadElementAutoSelector = new ThreadElementAutoSelector();

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

        threadSeekBar.start();
        threadElementAutoSelector.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ThreadElementAutoSelector.isStoped=true;
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
                if (isExpanded) {
                    shrinkSong();
                } else {
                    this.finish();
                }
                return true;
            }
        }
        return false;
    }

    private void expandSong() {
        isExpanded = true;
        ConstraintLayout constraintLayout = findViewById(R.id.main_layout);
        initialSet.clone(constraintLayout);
        ConstraintSet expandedSongSet = new ConstraintSet();
        expandedSongSet.clone(this, R.layout.song_view);

        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setDuration(400);

        Fade fade = new Fade();
        fade.setDuration(400);
        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(changeBounds);
        transitionSet.addTransition(fade);

        transitionSet.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                ThreadSeekBar.isRunning = true;
                ThreadElementAutoSelector.isRunning = false;
            }

            @Override
            public void onTransitionCancel(Transition transition){
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });

        TransitionManager.beginDelayedTransition(constraintLayout,transitionSet);
        expandedSongSet.applyTo(constraintLayout);
    }
    private void shrinkSong() {
        isExpanded=false;
        ConstraintLayout constraintLayout = findViewById(R.id.main_layout);

        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setDuration(400);

        Fade fade = new Fade();
        fade.setDuration(400);
        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(changeBounds);
        transitionSet.addTransition(fade);

        transitionSet.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                ThreadSeekBar.isRunning = false;

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                ThreadElementAutoSelector.isRunning = true;
            }

            @Override
            public void onTransitionCancel(Transition transition){
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });

        TransitionManager.beginDelayedTransition(constraintLayout,transitionSet);
        initialSet.applyTo(constraintLayout);
    }

    private void showSong(){

    }

    @Override
    public void onSongChanged(Song song) {
        btn_expand_play_song.setText(song.getSongName());
        artist_view.setText(song.getArtist());
        btn_play.setImageResource(R.drawable.pause_24dp);
    }
}




