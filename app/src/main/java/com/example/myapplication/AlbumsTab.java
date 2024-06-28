package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.myapplication.db.DatabaseHelper;
import com.example.myapplication.db.DatabaseManager;
import com.example.myapplication.interfaces.OnAlbumDeleteListener;
import com.example.myapplication.interfaces.ThreadGenerateAlbumsListener;
import com.example.myapplication.obj.Album;
import com.example.myapplication.obj.Song;

import java.io.File;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlbumsTab extends Fragment implements OnAlbumDeleteListener, ThreadGenerateAlbumsListener {
    Context context;
    LayoutInflater inflater;
    GridLayout gridLayout;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        PlaylistView.setOnAlbumDeleteListener(this);
        ThreadGenerateAlbums.setThreadGenerateAlbumsListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        View view = inflater.inflate(R.layout.fragment_albums, container, false);
        gridLayout = view.findViewById(R.id.album_card_container);
        if(ThreadGenerateAlbums.isFinished)
            updateAlbumLayout();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(ThreadGenerateAlbums.isFinished)
            updateAlbumLayout();
    }

    private List<Song> getListOfSongsWithAlbums(DatabaseManager databaseManager){
        List<Song> SongList = new ArrayList<>();
        Cursor cursor = databaseManager.fetchSongsWithAlbums();
        if(cursor.moveToFirst()){
            do{
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
        return SongList;
    }

    private void updateAlbumLayout() {
        gridLayout.removeAllViews();
        DatabaseManager databaseManager = new DatabaseManager(context);
        try {
            databaseManager.open();
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
        Cursor cursor = databaseManager.fetchAlbums();

        MainActivity mainActivity = (MainActivity) getActivity();

        if(cursor.moveToFirst()){
            do {
                @SuppressLint("Range") int ID = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ALBUM_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ALBUM_NAME));
                @SuppressLint("Range") String artist = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ALBUM_ARTIST));
                @SuppressLint("Range") String image = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ALBUM_PICTURE));

                CardView cardView = (CardView) inflater.inflate(R.layout.album_card, gridLayout, false);
                TextView albumID = cardView.findViewById(R.id.albumID);
                albumID.setText(String.valueOf(ID));
                TextView albumName = cardView.findViewById(R.id.album_card_name);
                albumName.setText(name);
                TextView albumArtist = cardView.findViewById(R.id.album_card_artist);
                albumArtist.setText(artist);
                ImageView backgroundImage = cardView.findViewById(R.id.album_card_background_image);
                ImageView cardImage = cardView.findViewById(R.id.album_card_image);

                if (!image.equals("placeholder.png") && mainActivity != null) {
                    Bitmap bitmap = mainActivity.loadImageFromStorage(name);
                    Bitmap bitmapBlur = mainActivity.loadImageFromStorage(name+"_blur");

                    cardImage.setImageBitmap(bitmap);
                    backgroundImage.setImageBitmap(bitmapBlur);
                }



                View.OnClickListener album_id_listener = v -> {
                    TextView textView = v.findViewById(R.id.albumID);
                    int id = Integer.parseInt((String) textView.getText());
                    TextView nameTextView = v.findViewById(R.id.album_card_name);
                    String nameText = (String) nameTextView.getText();
                    if (mainActivity != null) {
                        updateAlbumContent(id,nameText);
                        mainActivity.getPlaylistView().openAlbum(id);
                    }
                };

                cardView.setOnClickListener(album_id_listener);
                gridLayout.addView(cardView);

            }while (cursor.moveToNext());
        }
        cursor.close();
        databaseManager.close();
    }

    private void updateAlbumContent(int id,String album_name){
        DatabaseManager databaseManager = new DatabaseManager(context);
        try {
            databaseManager.open();

        List<Song> SongList = getListOfSongsWithAlbums(databaseManager);
        Cursor cursor = databaseManager.fetchAlbumsSongsFullInfo(id,"title");
        SongList.removeIf(s -> !s.getAlbum().equals(album_name));


        if(cursor.moveToFirst()){
            do {
                @SuppressLint("Range") int song_id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SONG_ID));
                SongList.removeIf(s -> s.getId()==song_id);
            }while (cursor.moveToNext());
        }
        cursor.close();

        for(Song s : SongList){
            databaseManager.insertAlbumSong(id,s.getId());
        }


        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
        finally {
            databaseManager.close();
        }
    }

    @Override
    public void OnAlbumDelete() {
        if(ThreadGenerateAlbums.isFinished)
            updateAlbumLayout();
    }

    @Override
    public void OnThreadFinish() {
        updateAlbumLayout();
    }
}
