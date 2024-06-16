package com.example.myapplication;

import static com.example.myapplication.MyMediaPlayer.CurrentIndex;
import static com.example.myapplication.MyMediaPlayer.playMedia;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;

import com.example.myapplication.db.DatabaseHelper;
import com.example.myapplication.db.DatabaseManager;
import com.example.myapplication.obj.Song;

import java.io.File;
import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;

public class SongsTab extends Fragment {

    private List<Song> SongList;
    private MP3ListAdapter AudioAdapter;
    MediaPlayer mediaPlayer;
    private boolean isListSent = false;
    private boolean multi_select_mode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaPlayer = MyMediaPlayer.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_folder, container, false);

        ListView musicListView = view.findViewById(R.id.AudioList);

        SongList = new ArrayList<>();

        AudioAdapter = new MP3ListAdapter(getActivity(), SongList);
        loadAudio();
        musicListView.setAdapter(AudioAdapter);

        ImageButton btn_delete = view.findViewById(R.id.btn_delete_songs_fragment);

        SearchView searchBar = view.findViewById(R.id.search_bar_song_tab);
        searchBar.setOnClickListener(v -> searchBar.setIconified(false));

        //Garbage????
        ThreadElementAutoSelector.AudioAdapter = AudioAdapter;
        ThreadElementAutoSelector.SongList = SongList;

        AdapterView.OnItemClickListener itemListener = (parent, v, position, id) -> {
            sendData();
            playMedia(position);
            if(!multi_select_mode) checkSelection();
            else {
                MyMediaPlayer.instance.pause();
                SongList.get(position).setSelected(!SongList.get(position).isSelected());
                AudioAdapter.notifyDataSetChanged();
            }
        };
        musicListView.setOnItemClickListener(itemListener);

        musicListView.setOnItemLongClickListener((parent, view1, position, id) -> {
            ThreadElementAutoSelector.isRunning=false;
            multi_select_mode = true;
            btn_delete.setVisibility(View.VISIBLE);
            return false;
        });

        btn_delete.setOnClickListener(this::showDeleteDialog);

        return view;
    }

    private void showDeleteDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        int count=0;
        for(Song s: SongList){
            if(s.isSelected()) count++;
        }
        builder.setMessage("Are you sure you want to delete " + count+" songs?");
        builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                    DatabaseManager databaseManager = new DatabaseManager(getContext());
                    try {
                        databaseManager.open();

                        for (Song s : SongList) {
                            if (s.isSelected()) {
                                databaseManager.deleteSong(s.getId());
                            }
                        }

                        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        File[] files = null;
                        if (downloadsFolder.exists() && downloadsFolder.isDirectory()) {
                            files = downloadsFolder.listFiles();
                            if (files != null) {
                                for (File file : files)
                                    for (Song s : SongList)
                                        if (file.isFile() && s.isSelected() && file.getName().equals(s.getFilename())) {
                                            file.delete();
                                            break;
                                        }

                            }
                        }

                        SongList.removeIf(Song::isSelected);
                    } catch (SQLDataException e) {
                        throw new RuntimeException(e);
                    } finally {
                        AudioAdapter.notifyDataSetChanged();
                        cleanUp();
                        databaseManager.close();
                    }
            dialog.dismiss();
        });

        builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
            cleanUp();
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        v.setVisibility(View.GONE);
    }

    private void cleanUp() {
        multi_select_mode = false;
        isListSent=false;
        sendData();
        ThreadElementAutoSelector.isRunning=true;
        MyMediaPlayer.setSongList(SongList);
        MyMediaPlayer.CurrentIndex=0;
        MyMediaPlayer.playMedia(CurrentIndex);
        mediaPlayer.pause();
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
        for(Song s : SongList) {
            s.setSelected(currentSongId == s.getId());
            break;
        }
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
        cursor.close();
        AudioAdapter.notifyDataSetChanged();

        databaseManager.close();
    }

}