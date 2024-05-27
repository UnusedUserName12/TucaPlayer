package com.example.myapplication;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.myapplication.obj.Song;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
/*
public class SongsTabController {

    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    private final List<Song> AudioList;
    private final MP3ListAdapter AudioAdapter;
    private final ImageButton play_button;
    private final TextView currentMediaText;
    private static boolean OnRepeat=false, OnShuffle=false;


    public SongsTabController(Fragment folderFragment){

        AudioList = ((SongsTab)folderFragment).getSongList();
        AudioAdapter = ((SongsTab)folderFragment).getAudioAdapter();
        ListView musicListView = ((SongsTab)folderFragment).getMusicListView();

        AdapterView.OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                playMedia(position);
            }
        };
        musicListView.setOnItemClickListener(itemListener);

        play_button = ((SongsTab)folderFragment).getPlay_pause_button();
        currentMediaText = ((SongsTab)folderFragment).getCurrentMediaText();

        play_button.setOnClickListener(v -> {
            if(!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                play_button.setImageResource(R.drawable.pause);
            }
            else{
                mediaPlayer.pause();
                play_button.setImageResource(R.drawable.play);
            }
        });


        ImageButton skip_forward = ((SongsTab)folderFragment).getSkip_forward_button();
        skip_forward.setOnClickListener(v -> {
            if (MyMediaPlayer.CurrentIndex == AudioList.size()-1){
                MyMediaPlayer.CurrentIndex=-1;
            }
            else if(OnShuffle){
                Random ran = new Random(System.currentTimeMillis());
                MyMediaPlayer.CurrentIndex = ran.nextInt(AudioList.size());
            }
            else {
                MyMediaPlayer.CurrentIndex = MyMediaPlayer.CurrentIndex + 1;
            }
            playMedia(MyMediaPlayer.CurrentIndex);
        });


        ImageButton skip_back = ((SongsTab)folderFragment).getSkip_backward_button();
        skip_back.setOnClickListener(v -> {
            if (MyMediaPlayer.CurrentIndex == 0){
                MyMediaPlayer.CurrentIndex=AudioList.size();
            }
            if(MyMediaPlayer.CurrentIndex!=-1) {
                mediaPlayer.reset();
                --MyMediaPlayer.CurrentIndex;
            }
            playMedia(MyMediaPlayer.CurrentIndex);
        });


        ImageButton repeat_button = ((SongsTab)folderFragment).getRepeat_button();
        repeat_button.setOnClickListener(v -> {
            if(!OnRepeat){
                OnRepeat=true;
                repeat_button.setBackground(new ColorDrawable(Color.rgb(240, 240, 240)));
            }
            else {
                OnRepeat=false;
                repeat_button.setBackground(new ColorDrawable(Color.WHITE));
            }
        });


        ImageButton shuffle_button = ((SongsTab)folderFragment).getShuffle_button();
        shuffle_button.setOnClickListener(v -> {
            if(!OnShuffle){
                OnShuffle=true;
                shuffle_button.setBackground(new ColorDrawable(Color.rgb(240, 240, 240)));
            }
            else {
                OnShuffle=false;
                shuffle_button.setBackground(new ColorDrawable(Color.WHITE));
            }
        });

        SeekBar seekBar = ((SongsTab)folderFragment).getSeekBar();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer!=null && fromUser){
                    mediaPlayer.seekTo(progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void playMedia(int position){
        mediaPlayer.reset();
        MyMediaPlayer.CurrentIndex = position;

        try {
            String selectedAudioFile = AudioList.get(position).getFilename();

            File audioFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), selectedAudioFile);
            currentMediaText.setText(String.format("Playing: %s", audioFile.getName()));
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            play_button.setImageResource(R.drawable.pause);

            mediaPlayer.setOnCompletionListener(mp -> {
                play_button.setImageResource(R.drawable.play);
                if (!OnRepeat) {
                    if (OnShuffle) {
                        Random ran = new Random(System.currentTimeMillis());
                        MyMediaPlayer.CurrentIndex = ran.nextInt(AudioList.size());
                    } else {
                        MyMediaPlayer.CurrentIndex++;
                    }
                }
                playMedia(MyMediaPlayer.CurrentIndex);
            });

            if (MP3ListAdapter.selectedItemPosition != -1) {
                AudioList.get(MP3ListAdapter.selectedItemPosition).setSelected(false);
            }

            MP3ListAdapter.selectedItemPosition = MyMediaPlayer.CurrentIndex;
            AudioList.get(MP3ListAdapter.selectedItemPosition).setSelected(true);
            AudioAdapter.notifyDataSetChanged();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
*/

