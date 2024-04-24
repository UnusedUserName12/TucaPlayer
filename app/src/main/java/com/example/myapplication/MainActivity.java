package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;
    /*
    private ListView musicListView;
    private List<String> AudioList;
    private MP3ListAdapter AudioAdapter;
    private ImageButton play_pause_button;
    private ImageButton skip_forward_button;
    private ImageButton skip_backward_button;
    private ImageButton repeat_button;
    private ImageButton shuffle_button;
    private TextView currentMediaText;
    private SeekBar seekBar;
    private TextView currentTime, totalTime;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
*/
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    ViewPagerAdapter viewPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                tabLayout.getTabAt(position).select();
            }
        });
        /*
        musicListView = findViewById(R.id.AudioList);

        AudioList = new ArrayList<>();
        AudioAdapter = new MP3ListAdapter(this, AudioList);
        musicListView.setAdapter(AudioAdapter);


         */

        if (checkPermission()) {
            //loadAudio();
        } else {
            requestPermission();
            //loadAudio();
        }


        /*
        play_pause_button = findViewById(R.id.play_pause);
        skip_forward_button = findViewById(R.id.next);
        skip_backward_button = findViewById(R.id.back);
        repeat_button = findViewById(R.id.repeat);
        shuffle_button = findViewById(R.id.shuffle);
        currentMediaText = findViewById(R.id.currentMedia);

        seekBar = findViewById(R.id.seek_bar);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);

        MainActivity.this.runOnUiThread(new Runnable(){
            @Override
            public void run()
            {
                if(mediaPlayer!=null){
                    seekBar.setMax(mediaPlayer.getDuration());
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    totalTime.setText(convertToMMSS(mediaPlayer.getDuration()));
                    currentTime.setText(convertToMMSS(mediaPlayer.getCurrentPosition()));
                }
                new Handler().postDelayed(this,100);
            }
        });

        ControllerMainActivity controller = new ControllerMainActivity(this);
        */
    }

/*
    public static String convertToMMSS(long duration){
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
*/
    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //loadAudio();
            }
        }
    }

    /*
    private void loadAudio() {
        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloadsFolder.exists() && downloadsFolder.isDirectory()) {
            File[] files = downloadsFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".mp3")) {
                        AudioList.add(file.getName());
                    }
                }
                AudioAdapter.notifyDataSetChanged();
            }
        } else {
            Toast.makeText(this, "Downloads folder not found", Toast.LENGTH_SHORT).show();
        }
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

    public List<String> getAudioList() {
        return AudioList;
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

     */

}




