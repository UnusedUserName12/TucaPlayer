package com.example.myapplication;

import static com.example.myapplication.MyMediaPlayer.playMedia;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.myapplication.db.DatabaseHelper;
import com.example.myapplication.db.DatabaseManager;
import com.example.myapplication.obj.Song;

import java.util.ArrayList;
import java.util.List;

public class SongsTab extends Fragment {

    private ListView musicListView;
    private List<Song> SongList;
    private MP3ListAdapter AudioAdapter;
    MediaPlayer mediaPlayer;
    private boolean isListSent = false;

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

        //Garbage????
        ThreadElementAutoSelector.AudioAdapter = AudioAdapter;
        ThreadElementAutoSelector.SongList = SongList;

        AdapterView.OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                sendData();
                playMedia(position);
                checkSelection();
            }
        };
        musicListView.setOnItemClickListener(itemListener);

        return view;
    }
    /**
     * Sends the song list and updates the UI with the playlist image
     <ul>
     *     <li>Sets the song list in the MyMediaPlayer.</li>
     *     <li>Updates the last playlist ID in the settings to 0.</li>
     *     <li>Sets image in the UI to placeholder.</li>
     * </ul>
     *
     * <p>The method ensures that these actions are only performed once by checking the {@code isListSent} flag.
     * If the list has already been sent, the method will not perform any actions.
     */
    private void sendData(){
        if(!isListSent) {
            MyMediaPlayer.setSongList(SongList);
            MainActivity mainActivity = (MainActivity) getActivity();
            if(mainActivity != null) {
                mainActivity.settings.setLast_playlist_id(0);
                ImageView song_view_image = mainActivity.findViewById(R.id.song_view_image);
                song_view_image.setImageResource(R.drawable.placeholder);
            }
        }
        isListSent = true;
    }


    private void checkSelection(){
        int currentSongId = MyMediaPlayer.getCurrentSongId();
        for(Song s : SongList) s.setSelected(currentSongId == s.getId());
        AudioAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        isListSent=false;
        ThreadElementAutoSelector.AudioAdapter = AudioAdapter;
        ThreadElementAutoSelector.SongList = SongList;
    }
    
    //Now uses database values
    private void loadAudio() {
        DatabaseManager databaseManager = new DatabaseManager(getContext());
        try{
            databaseManager.open();
        }catch (Exception e){
            e.printStackTrace();
        }

        Cursor cursor = databaseManager.fetchSongs("title");
        if(cursor.moveToFirst()){
            do {
                @SuppressLint("Range") int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ID)));
                @SuppressLint("Range") String filename = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_FILENAME));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_NAME));
                @SuppressLint("Range") String artist = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ARTIST));
                @SuppressLint("Range") String album = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ALBUM));
                @SuppressLint("Range") String genre = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_GENRE));
                @SuppressLint("Range") long duration = Long.parseLong(cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_DURATION)));

                Song song = new Song(id,filename,name,artist,album,genre,duration);
                SongList.add(song);

            }while (cursor.moveToNext());
        }
        AudioAdapter.notifyDataSetChanged();

        databaseManager.close();
    }

}