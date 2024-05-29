package com.example.myapplication;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import com.example.myapplication.obj.Song;

import java.util.List;
/**
 * A thread that automatically selects the currently playing song from a list of songs.
 * It updates the selection state of the songs in the list and notifies the adapter of changes.
 */
public class ThreadElementAutoSelector extends Thread{
    static List<Song> SongList;
    static MP3ListAdapter AudioAdapter;
    static int current_pos;
    static int prev_pos;
    static boolean isRunning;
    private final Handler handler;
    static MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();

    /**
     * Constructs a new ThreadElementAutoSelector with default values.
     * Initializes the song list, adapter, positions, and handler.
     */
    ThreadElementAutoSelector(){
        AudioAdapter = null;
        SongList = null;
        current_pos=0;
        prev_pos=-1;
        isRunning = true;
        this.handler = new Handler(Looper.getMainLooper());
    }

    /**
     * Constructs a new ThreadElementAutoSelector with the specified song list and adapter.
     * Initializes the song list, adapter, positions, and handler.
     *
     * @param songList the list of songs to be managed by this thread
     * @param audioAdapter the adapter for the list of songs
     */
    ThreadElementAutoSelector(List<Song> songList, MP3ListAdapter audioAdapter){
        SongList = songList;
        AudioAdapter = audioAdapter;
        current_pos=0;
        prev_pos=-1;
        isRunning = true;
        this.handler = new Handler(Looper.getMainLooper());
    }

    /**
     * Runs the thread, which continuously checks the currently playing song and updates
     * the selection state in the song list. The adapter is notified of changes to update the display.
     */
    public void run() {
        while (true) {
        if (AudioAdapter != null && !SongList.isEmpty()) {
                if (mediaPlayer != null && isRunning) {
                    handler.post(() -> {
                        int currentSongId = MyMediaPlayer.getCurrentSongId();
                        for (Song s : SongList)
                            if (currentSongId == s.getId()) {
                                s.setSelected(true);
                                current_pos = SongList.indexOf(s);
                            }
                        if (prev_pos > -1 && prev_pos != current_pos)
                            SongList.get(prev_pos).setSelected(false);
                        AudioAdapter.notifyDataSetChanged();
                        prev_pos = current_pos;
                    });
                }

            }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        }
    }
}
