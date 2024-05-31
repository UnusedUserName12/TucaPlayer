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
    static boolean isRunning;
    static boolean isStopped = false;
    private final Handler handler;
    static MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();

    /**
     * Constructs a new ThreadElementAutoSelector with default values.
     * Initializes the song list, adapter, positions, and handler.
     */
    ThreadElementAutoSelector(){
        SongList = MyMediaPlayer.getSongList();
        AudioAdapter = null;
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
        isRunning = true;
        this.handler = new Handler(Looper.getMainLooper());
    }

    /**
     * Runs the thread, which continuously checks the currently playing song and updates
     * the selection state in the song list. The adapter is notified of changes to update the display.
     */
    public void run() {
        while (!isStopped) {
        if (AudioAdapter != null && SongList != null) {
                if (mediaPlayer != null && isRunning) {
                    handler.post(() -> {
                        int currentSongId = MyMediaPlayer.getCurrentSongId();
                        for(Song s : SongList) s.setSelected(currentSongId == s.getId());
                        AudioAdapter.notifyDataSetChanged();
                    });
                }

            }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        }
        isStopped =false;
    }
}
