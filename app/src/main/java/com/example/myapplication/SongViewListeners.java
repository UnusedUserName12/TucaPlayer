package com.example.myapplication;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.myapplication.interfaces.OnSongChangeListener;
import com.example.myapplication.obj.Song;

import java.util.Random;

/**
 * Class that implements listeners for song view controls in the MainActivity.
 * This class manages the user interactions with the song_view.
 */
public class SongViewListeners implements OnSongChangeListener {
    private final MainActivity mainActivity;
    static MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    private final ConstraintSet initialSet;
    private final ConstraintSet expandedSongSet;
    private final ImageView btnPlay;
    private final TextView song_name_text_view;
    private final TextView artist_view;
    boolean isExpanded = false;
    /**
     * Constructor to initialize the SongViewListeners with required parameters.
     *
     * @param mainActivity      The MainActivity instance.
     * @param initialSet        The initial ConstraintSet configuration.
     * @param expandedSongSet   The expanded ConstraintSet configuration.
     * @param btnPlay           The play button ImageView.
     * @param songNameTextView  The TextView for displaying the song name.
     * @param artistView        The TextView for displaying the artist name.
     */
    public SongViewListeners(MainActivity mainActivity, ConstraintSet initialSet, ConstraintSet expandedSongSet, ImageView btnPlay, TextView songNameTextView, TextView artistView) {
        this.mainActivity = mainActivity;
        this.initialSet = initialSet;
        this.expandedSongSet = expandedSongSet;
        this.btnPlay = btnPlay;
        song_name_text_view = songNameTextView;
        artist_view = artistView;
    }
    /**
     * Sets up listeners for various UI controls related to song playback.
     *
     * @param btnPlay       The play button ImageView.
     * @param btnRepeat     The repeat button ImageView.
     * @param btnNext       The next song button ImageView.
     * @param btnBack       The previous song button ImageView.
     * @param btnShuffle    The shuffle button ImageView.
     * @param seekBar       The SeekBar for song progress.
     * @param bottom_panel  The LinearLayout that serves as the bottom panel. Clicking on it will expand the song_view
     * @param btn_shrink    The ImageView for shrinking the song view.
     */
    public void setListeners(ImageView btnPlay, ImageView btnRepeat, ImageView btnNext, ImageView btnBack, ImageView btnShuffle,
                             SeekBar seekBar, LinearLayout bottom_panel, ImageView btn_shrink) {
        btnPlay.setOnClickListener(v -> {
            if (!mediaPlayer.isPlaying()) {
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
            } else {
                MyMediaPlayer.onRepeat = false;
                btnRepeat.setBackground(new ColorDrawable(Color.WHITE));
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
                if (MyMediaPlayer.CurrentIndex == 0) {
                    MyMediaPlayer.CurrentIndex = MyMediaPlayer.getSongListSize();
                }
                if (MyMediaPlayer.CurrentIndex != -1) {
                    MyMediaPlayer.CurrentIndex--;
                }
                MyMediaPlayer.playMedia(MyMediaPlayer.CurrentIndex);
            }
        });

        btnShuffle.setOnClickListener(v -> {
            if (!MyMediaPlayer.onShuffle) {
                MyMediaPlayer.onShuffle = true;
                btnShuffle.setBackground(new ColorDrawable(Color.rgb(240, 240, 240)));
            } else {
                MyMediaPlayer.onShuffle = false;
                btnShuffle.setBackground(new ColorDrawable(Color.WHITE));
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

        song_name_text_view.setOnClickListener(v -> {
            if (!isExpanded) {
                expandSong();
            }
        });

        btn_shrink.setOnClickListener(v -> {
            if (isExpanded) {
                shrinkSong();
            }
        });



        MyMediaPlayer.setOnSongChangeListener(this);
    }

    private void expandSong() {
        isExpanded = true;
        ConstraintLayout constraintLayout = mainActivity.findViewById(R.id.main_layout);

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
        expandedSongSet.applyTo(constraintLayout);
    }

    void shrinkSong() {
        isExpanded = false;
        ConstraintLayout constraintLayout = mainActivity.findViewById(R.id.main_layout);

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
        initialSet.applyTo(constraintLayout);
    }
    /**
     * Callback method when the song is changed.
     * Updates the UI with the new song details.
     *
     * @param song The new song that is being played.
     */
    @Override
    public void onSongChanged(Song song) {
        song_name_text_view.setText(song.getSongName());
        artist_view.setText(song.getArtist());
        btnPlay.setImageResource(R.drawable.pause_24dp);
    }
}

