package com.example.myapplication;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.myapplication.db.DatabaseHelper;
import com.example.myapplication.db.DatabaseManager;
import com.example.myapplication.obj.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SongsTab extends Fragment {

    private ListView musicListView;
    private List<Song> SongList;
    private MP3ListAdapter AudioAdapter;
    private ImageButton play_pause_button;
    private ImageButton skip_forward_button;
    private ImageButton skip_backward_button;
    private ImageButton repeat_button;
    private ImageButton shuffle_button;
    private TextView currentMediaText;
    private SeekBar seekBar;
    private TextView currentTime, totalTime;
    MediaPlayer mediaPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaPlayer = MyMediaPlayer.getInstance();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_folder, container, false);

        musicListView = view.findViewById(R.id.AudioList);

        SongList = new ArrayList<>();

        AudioAdapter = new MP3ListAdapter(getActivity(), SongList);
        loadAudio();
        musicListView.setAdapter(AudioAdapter);

        play_pause_button = view.findViewById(R.id.play_pause);
        skip_forward_button = view.findViewById(R.id.next);
        skip_backward_button = view.findViewById(R.id.back);
        repeat_button = view.findViewById(R.id.repeat);
        shuffle_button = view.findViewById(R.id.shuffle);
        currentMediaText = view.findViewById(R.id.currentMedia);

        seekBar = view.findViewById(R.id.seek_bar);
        currentTime = view.findViewById(R.id.currentTime);
        totalTime = view.findViewById(R.id.totalTime);


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    seekBar.setMax(mediaPlayer.getDuration());
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    totalTime.setText(convertToMMSS(mediaPlayer.getDuration()));
                    currentTime.setText(convertToMMSS(mediaPlayer.getCurrentPosition()));
                }
                new Handler().postDelayed(this, 100);
            }
        });

        SongsTabController controller = new SongsTabController(this);

        return view;
    }

    public static String convertToMMSS(long duration){
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    //Now uses database values
    private void loadAudio() {
        DatabaseManager databaseManager = new DatabaseManager(getContext());
        try{
            databaseManager.open();
        }catch (Exception e){
            e.printStackTrace();
        }

        Cursor cursor = databaseManager.fetchSongs();
        if(cursor.moveToFirst()){
            do {
                @SuppressLint("Range") String filename = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_FILENAME));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_NAME));
                @SuppressLint("Range") String artist = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ARTIST));
                @SuppressLint("Range") String album = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ALBUM));
                @SuppressLint("Range") String genre = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_GENRE));

                Song song = new Song(filename,name,artist,album);
                SongList.add(song);

            }while (cursor.moveToNext());
        }
        AudioAdapter.notifyDataSetChanged();

        databaseManager.close();


    }

    public ImageButton getPlay_pause_button() {
        return play_pause_button;
    }

    public ImageButton getSkip_forward_button() {
        return skip_forward_button;
    }

    public ImageButton getSkip_backward_button() {
        return skip_backward_button;
    }

    public ImageButton getRepeat_button() {
        return repeat_button;
    }

    public ImageButton getShuffle_button() {
        return shuffle_button;
    }

    public List<Song> getSongList() {
        return SongList;
    }

    public TextView getCurrentMediaText() {
        return currentMediaText;
    }

    public MP3ListAdapter getAudioAdapter() {
        return AudioAdapter;
    }

    public SeekBar getSeekBar() {
        return seekBar;
    }

    public ListView getMusicListView() {
        return musicListView;
    }

}