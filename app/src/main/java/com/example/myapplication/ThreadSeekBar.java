package com.example.myapplication;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class ThreadSeekBar extends Thread {
    private SeekBar seekBar;
    private TextView totalTime;
    private TextView currentTime;
    static MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    static boolean isRunning;
    private final Handler handler;
    static boolean isStopped = false;

    ThreadSeekBar(SeekBar seekBar, TextView totalTime, TextView currentTime) {
        this.seekBar = seekBar;
        this.totalTime = totalTime;
        this.currentTime = currentTime;
        isRunning = false;
        this.handler = new Handler(Looper.getMainLooper());
    }
    public static String convertToMMSS(long duration){
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    public void run(){
        while (!isStopped) {
            if (mediaPlayer.isPlaying() && isRunning) {
                handler.post(() -> {
                    long duration = mediaPlayer.getDuration();
                    long current_pos = mediaPlayer.getCurrentPosition();
                    seekBar.setMax((int) duration);
                    seekBar.setProgress((int) current_pos);
                    totalTime.setText(convertToMMSS(duration));
                    currentTime.setText(convertToMMSS(current_pos));
                });
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
        isStopped = false;
    }
}
