package com.example.myapplication;

import android.media.MediaPlayer;
import android.os.Environment;

import com.example.myapplication.interfaces.OnSongChangeListener;
import com.example.myapplication.obj.Song;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class MyMediaPlayer {
    static MediaPlayer instance;

    public static MediaPlayer getInstance(){
        if(instance == null){
            instance = new MediaPlayer();
        }
        return instance;
    }

    public static int CurrentIndex = -1;
    private static int currentSongId;
    private static String currentSongName;
    public static boolean onRepeat = false;
    public static boolean onShuffle = false;
    private static OnSongChangeListener songChangeListener;
    private static List<Song> SongList;
    public static void setSongList(List<Song> songList) {
        //TODO: Test changes in this function
        //instance.reset();
        SongList = songList;
    }
    public static List<Song> getSongList() {
        return SongList;
    }

    public static int getSongListSize() {
        return SongList.size();
    }
    public static int getCurrentSongId() {return currentSongId;}


    /**
     * Plays a media file from the SongList at the given position.
     * <p>
     * This method resets the media player instance, sets the current index to the specified position,
     * and attempts to play the audio file located in the public downloads directory.
     * If the file is successfully played, an OnCompletionListener is set to handle what happens when
     * the media file completes playing. The listener supports both shuffle and repeat functionalities.
     * </p>
     *
     * @param position the index of the audio file in the SongList to be played.
     */
    public static void playMedia(int position){
        instance.reset();
        CurrentIndex = position;

        try {
            String selectedAudioFile = SongList.get(position).getFilename();
            currentSongId = SongList.get(position).getId();

            if (songChangeListener != null) {
                songChangeListener.onSongChanged(SongList.get(position));
            }

            File audioFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), selectedAudioFile);
            instance.setDataSource(audioFile.getAbsolutePath());
            instance.prepare();
            instance.start();

            instance.setOnCompletionListener(mp -> {
                if (!onRepeat) {
                    if (onShuffle) {
                        Random ran = new Random(System.currentTimeMillis());
                        CurrentIndex = ran.nextInt(SongList.size());
                    } else {
                        if(CurrentIndex < SongList.size()-1) CurrentIndex++;
                        else CurrentIndex=0;
                    }
                }
                playMedia(CurrentIndex);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setOnSongChangeListener(OnSongChangeListener listener) {
        songChangeListener = listener;
    }
}
