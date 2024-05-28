package com.example.myapplication;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class ThreadSeekBar extends Thread{
    SeekBar seekBar;
    TextView totalTime;
    TextView currentTime;
    static MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    public boolean isRunning;
    private final Handler handler;

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
        while (true) {
            if (mediaPlayer != null && isRunning) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                handler.post(() -> {

                    seekBar.setMax(mediaPlayer.getDuration());
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    totalTime.setText(convertToMMSS(mediaPlayer.getDuration()));
                    currentTime.setText(convertToMMSS(mediaPlayer.getCurrentPosition()));
                });
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
    public void setRunning(boolean running) {
        isRunning = running;
    }
}
