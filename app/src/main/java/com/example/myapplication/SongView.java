package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Environment;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.myapplication.db.DatabaseHelper;
import com.example.myapplication.db.DatabaseManager;
import com.example.myapplication.interfaces.OnSongChangeListener;
import com.example.myapplication.obj.Song;

import java.io.File;
import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Class that implements listeners for song view controls in the MainActivity.
 * This class manages the user interactions with the song_view.
 */
public class SongView implements OnSongChangeListener {
    private final MainActivity mainActivity;
    static MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    ConstraintLayout constraintLayout;
    private ImageView btnPlay;
    private ImageView btnFavorite;
    private TextView song_name_view;
    private TextView artist_view;
    static boolean isExpanded = false;
    private List<Song> SongList;
    private Song mSong;
    /**
     * Constructor to initialize the SongViewListeners with required parameters.
     *
     * @param mainActivity      The MainActivity instance.
     */
    public SongView(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        constraintLayout = mainActivity.findViewById(R.id.main_layout);

    }
    /**
     * Sets up listeners for various UI controls related to song playback.
     */
    public void setListeners() {
        btnPlay = mainActivity.findViewById(R.id.btn_play_pause);
        ImageView btnRepeat = mainActivity.findViewById(R.id.btn_repeat);
        ImageView btnNext = mainActivity.findViewById(R.id.btn_next);
        ImageView btnBack = mainActivity.findViewById(R.id.btn_back);
        ImageView btnShuffle = mainActivity.findViewById(R.id.btn_shuffle);
        ImageView btnShrink = mainActivity.findViewById(R.id.btn_close_play_song);
        SeekBar seekBar = mainActivity.findViewById(R.id.seek_bar);
        LinearLayout bottom_panel = mainActivity.findViewById(R.id.empty_place);
        btnFavorite = mainActivity.findViewById(R.id.btn_favorite_song);
        ImageView btnMore = mainActivity.findViewById(R.id.btn_more_play_song);

        song_name_view = mainActivity.findViewById(R.id.song_view_name);
        artist_view = mainActivity.findViewById(R.id.artist_play_song);


        btnPlay.setOnClickListener(v -> {
            if(!mainActivity.settings.isSong_playing() && !mediaPlayer.isPlaying()) {
                MyMediaPlayer.playMedia(MyMediaPlayer.CurrentIndex);
                btnPlay.setImageResource(R.drawable.pause_24dp);
                mainActivity.settings.setSong_playing(true);
            }
            else if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                btnPlay.setImageResource(R.drawable.pause_24dp);
            } else {
                mediaPlayer.pause();
                btnPlay.setImageResource(R.drawable.play_arrow_24dp);
            }
        });

        btnRepeat.setOnClickListener(v -> {
            if (!MyMediaPlayer.onRepeat) {
                MyMediaPlayer.onRepeat = true;
                btnRepeat.setBackground(new ColorDrawable(Color.rgb(240, 240, 240)));
                mainActivity.settings.setSong_is_on_repeat(true);
            } else {
                MyMediaPlayer.onRepeat = false;
                btnRepeat.setBackground(new ColorDrawable(Color.TRANSPARENT));
                mainActivity.settings.setSong_is_on_repeat(false);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (MyMediaPlayer.getSongList() != null) {
                if (MyMediaPlayer.CurrentIndex == MyMediaPlayer.getSongListSize() - 1) {
                    MyMediaPlayer.CurrentIndex = 0;
                } else if (MyMediaPlayer.onShuffle) {
                    Random ran = new Random(System.currentTimeMillis());
                    MyMediaPlayer.CurrentIndex = ran.nextInt(MyMediaPlayer.getSongListSize());
                } else {
                    MyMediaPlayer.CurrentIndex++;
                }
                MyMediaPlayer.playMedia(MyMediaPlayer.CurrentIndex);
            }
        });

        btnBack.setOnClickListener(v -> {
            if (MyMediaPlayer.getSongList() != null) {
                if(MyMediaPlayer.instance.getCurrentPosition()<5000){
                    if (MyMediaPlayer.CurrentIndex == 0) {
                        MyMediaPlayer.CurrentIndex = MyMediaPlayer.getSongListSize()-1;
                    }
                    else if (MyMediaPlayer.CurrentIndex != -1) {
                        MyMediaPlayer.CurrentIndex--;
                    }
                }
                MyMediaPlayer.playMedia(MyMediaPlayer.CurrentIndex);
            }
        });

        btnShuffle.setOnClickListener(v -> {
            if (!MyMediaPlayer.onShuffle) {
                MyMediaPlayer.onShuffle = true;
                btnShuffle.setBackground(new ColorDrawable(Color.rgb(240, 240, 240)));
                mainActivity.settings.setSong_is_on_shuffle(true);
            } else {
                MyMediaPlayer.onShuffle = false;
                btnShuffle.setBackground(new ColorDrawable(Color.TRANSPARENT));
                mainActivity.settings.setSong_is_on_shuffle(false);
            }
        });

        btnFavorite.setOnClickListener(v -> {
            DatabaseManager databaseManager = new DatabaseManager(mainActivity);
            try {
                databaseManager.open();
                int id = mainActivity.settings.getLast_song_id();
                for(Song song : SongList){
                    if(song.getId()==id && !song.isFavorite()) {
                        btnFavorite.setImageResource(R.drawable.favorite_24dp_fill);
                        databaseManager.insertFavorite(song.getId());
                        song.setFavorite(true);
                        break;
                    }
                    else if(song.getId()==id && song.isFavorite()) {
                        btnFavorite.setImageResource(R.drawable.favorite_24dp_no_fill);
                        databaseManager.deleteFavorite(song.getId());
                        song.setFavorite(false);
                        break;
                }
            }
                //SongList = setListOfFavorites();
            } catch (SQLDataException e) {
                throw new RuntimeException(e);
            }
            finally {
                databaseManager.close();
            }

        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        bottom_panel.setOnClickListener(v -> {
            if (!isExpanded) {
                expandSong();
            }
        });

        btnShrink.setOnClickListener(v -> {
            if (isExpanded) {
                shrinkSong();
            }
        });

        btnMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(mainActivity, v);

            popupMenu.getMenuInflater().inflate(R.menu.song_options_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {

                String option = (String) menuItem.getTitle();
                assert option != null;

                option = option.toLowerCase();
                switch (Objects.requireNonNull(option)) {
                    case "edit":
                        break;
                    case "delete from device":
                        showDeleteDialog();
                        break;
                }
                return true;
            });
            popupMenu.show();
        });


        MyMediaPlayer.setOnSongChangeListener(this);
    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);

        builder.setMessage("Are you sure you want to delete " + mSong.getSongName() + "?");
        builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
            DatabaseManager databaseManager = new DatabaseManager(mainActivity);
            try {
                databaseManager.open();
                mediaPlayer.pause();
                List<Song> lSongList = MyMediaPlayer.getSongList();
                lSongList.removeIf(song -> song.getId() == mSong.getId());
                MyMediaPlayer.setSongList(lSongList);
                databaseManager.deleteSong(mSong.getId());

                File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File[] files = null;
                if (downloadsFolder.exists() && downloadsFolder.isDirectory()) {
                    files = downloadsFolder.listFiles();
                    if (files != null) {
                        for (File file : files)
                            if (file.isFile() && file.getName().equals(mSong.getFilename())){
                                file.delete();
                                break;
                            }

                    }
                }
                MyMediaPlayer.playMedia(MyMediaPlayer.CurrentIndex);
                mediaPlayer.pause();

            } catch (SQLDataException e) {
                throw new RuntimeException(e);
            } finally {
                databaseManager.close();
            }
            dialog.dismiss();
        });

        builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    void expandSong() {

        isExpanded = true;

        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setDuration(400);

        Fade fade = new Fade();
        fade.setDuration(400);
        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(changeBounds);
        transitionSet.addTransition(fade);

        transitionSet.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {}

            @Override
            public void onTransitionEnd(Transition transition) {
                ThreadSeekBar.isRunning = true;
                ThreadElementAutoSelector.isRunning = false;
            }

            @Override
            public void onTransitionCancel(Transition transition) {}

            @Override
            public void onTransitionPause(Transition transition) {}

            @Override
            public void onTransitionResume(Transition transition) {}
        });

        TransitionManager.beginDelayedTransition(constraintLayout, transitionSet);
        ConstraintSet expanded_song_set = new ConstraintSet();
        expanded_song_set.clone(mainActivity, R.layout.activity_main_song_view);
        expanded_song_set.applyTo(constraintLayout);

        SongList = setListOfFavorites();
        int last_id = mainActivity.settings.getLast_song_id();
        checkFavorite(last_id);
    }



    void shrinkSong() {
        isExpanded = false;

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
            public void onTransitionCancel(Transition transition) {}

            @Override
            public void onTransitionPause(Transition transition) {}

            @Override
            public void onTransitionResume(Transition transition) {}
        });

        TransitionManager.beginDelayedTransition(constraintLayout, transitionSet);
        if(!PlaylistView.isExpanded){
            ConstraintSet min_song_set = new ConstraintSet();
            min_song_set.clone(mainActivity, R.layout.activity_main);
            min_song_set.applyTo(constraintLayout);
        }
        else {
            ConstraintSet expandedPlaylistSet = new ConstraintSet();
            expandedPlaylistSet.clone(mainActivity, R.layout.activity_main_playlist_view);
            expandedPlaylistSet.applyTo(constraintLayout);
        }
    }

    void showSong(){
        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setDuration(400);

        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(changeBounds);

        transitionSet.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                ThreadElementAutoSelector.isRunning = false;
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                ThreadElementAutoSelector.isRunning = true;
            }

            @Override
            public void onTransitionCancel(Transition transition) {}

            @Override
            public void onTransitionPause(Transition transition) {}

            @Override
            public void onTransitionResume(Transition transition) {}
        });

        TransitionManager.beginDelayedTransition(constraintLayout, transitionSet);
        if(!PlaylistView.isExpanded) {
            ConstraintSet show_song_view_set = new ConstraintSet();
            show_song_view_set.clone(mainActivity, R.layout.activity_main);
            show_song_view_set.applyTo(constraintLayout);
        }
        else {
            ConstraintSet show_song_view_playlist_set = new ConstraintSet();
            show_song_view_playlist_set.clone(mainActivity, R.layout.activity_main_playlist_view);
            show_song_view_playlist_set.applyTo(constraintLayout);
        }
    }

    private List<Song> setListOfFavorites(){
        List<Song> SongList = MyMediaPlayer.getSongList();

        DatabaseManager databaseManager = new DatabaseManager(mainActivity);
        try {
            databaseManager.open();

            Cursor cursor = databaseManager.fetchFavorites("title");
            if(cursor.moveToFirst()){
                do {
                    @SuppressLint("Range") int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseHelper.FAVORITE_ID)));
                    for (Song song : SongList) {
                        if (song.getId() == id) {
                            song.setFavorite(true);
                            break;
                        }
                    }
                }while (cursor.moveToNext());
            }
            cursor.close();
            //MyMediaPlayer.setSongList(SongList);
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
        finally {
            databaseManager.close();
        }
        return SongList;
    }
    /**
     * Callback method when the song is changed.
     * Updates the UI with the new song details.
     *
     * @param song The new song that is being played.
     */
    @Override
    public void onSongChanged(Song song) {
        song_name_view.setText(song.getSongName());
        artist_view.setText(song.getArtist());
        btnPlay.setImageResource(R.drawable.pause_24dp);
        if(mainActivity.settings.getLast_song_id()<1) mainActivity.songView.showSong();

        if(SongList!=null){
            checkFavorite(song.getId());
        }
        mainActivity.settings.setLast_song_id(song.getId());
        mSong=song;
    }

    private void checkFavorite(int id) {
        for(Song song : SongList){
            if(song.getId() == id){
                if(song.isFavorite()) btnFavorite.setImageResource(R.drawable.favorite_24dp_fill);
                else btnFavorite.setImageResource(R.drawable.favorite_24dp_no_fill);
                break;
            }
        }
    }
}

