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
import com.example.myapplication.obj.Album;
import com.example.myapplication.obj.Song;

import java.io.File;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlbumsTab extends Fragment {
    Context context;
    LayoutInflater inflater;
    GridLayout gridLayout;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        View view = inflater.inflate(R.layout.fragment_albums, container, false);
        gridLayout = view.findViewById(R.id.album_card_container);
        updateAlbums();
        updateAlbumLayout();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAlbumLayout();
    }

    private void updateAlbums() {
        DatabaseManager databaseManager = new DatabaseManager(context);
        try {
            databaseManager.open();
            List<Song> SongList = getListOfSongsWithAlbums(databaseManager);
            List<Album> AlbumList = getListOfAlbums(databaseManager);
            List<Album> AlbumsToAdd = new ArrayList<>(AlbumList);

            if(AlbumList.isEmpty()){
                ArrayList<String> uniqueAlbums = new ArrayList<>();
                for(Song s : SongList) {
                    if(!uniqueAlbums.contains(s.getAlbum()))
                    {
                        uniqueAlbums.add(s.getAlbum());
                        AlbumsToAdd.add(new Album(0,s.getAlbum(),s.getArtist(),null));
                    }
                }
            }else {
                for (Song s : SongList) {
                    for (Album a : AlbumList) {
                        if (s.getAlbum().equals(a.getName())) {
                            AlbumsToAdd.remove(a);
                            break;
                        }
                    }
                }
            }

            //create new albums
            if (!AlbumsToAdd.isEmpty()) {
                List<Song> SongsToAddToNewAlbum = new ArrayList<>();
                for (Song s : SongList) {
                    for (Album a : AlbumsToAdd) {
                        if (s.getAlbum().equals(a.getName())) {
                            SongsToAddToNewAlbum.add(s);
                            break;
                        }
                    }
                }
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    for (Song s : SongsToAddToNewAlbum) {
                        File songFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), s.getFilename());
                        mmr.setDataSource(songFile.getPath());
                        byte[] data = mmr.getEmbeddedPicture();

                        if (data != null) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            mainActivity.saveToInternalStorage(bitmap, s.getAlbum());
                            databaseManager.insertAlbum(s.getAlbum(), s.getArtist(), s.getAlbum());
                        } else
                            databaseManager.insertAlbum(s.getAlbum(), s.getArtist(), "placeholder.png");
                    }
                }
            }


        }catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            databaseManager.close();
        }
    }

    private List<Album> getListOfAlbums(DatabaseManager databaseManager) {
        List<Album> AlbumList = new ArrayList<>();
        Cursor cursor = databaseManager.fetchAlbums();
        if(cursor.moveToFirst()){
            do{
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ALBUM_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ALBUM_NAME));
                @SuppressLint("Range") String artist = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ALBUM_ARTIST));
                @SuppressLint("Range") String image = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ALBUM_PICTURE));
                AlbumList.add(new Album(id,name,artist,image));

            }while (cursor.moveToNext());
        }
        cursor.close();
        return AlbumList;
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
                    Bitmap bitmapCopy = bitmap.copy(bitmap.getConfig(), true);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        backgroundImage.setRenderEffect(RenderEffect.createBlurEffect(22f, 22f, Shader.TileMode.CLAMP));
                    } else {
                        backgroundImage.setImageBitmap(blur(context, bitmapCopy));
                    }

                    cardImage.setImageBitmap(bitmap);
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

    public static Bitmap blur(Context context, Bitmap image) {
        Bitmap outputBitmap = Bitmap.createBitmap(image);
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation input = Allocation.createFromBitmap(rs, image);
        Allocation output = Allocation.createFromBitmap(rs, outputBitmap);
        blurScript.setRadius(22f);
        blurScript.setInput(input);
        blurScript.forEach(output);
        output.copyTo(outputBitmap);
        rs.destroy();
        return outputBitmap;
    }

    private void updateAlbumContent(int id,String album_name){
        DatabaseManager databaseManager = new DatabaseManager(context);
        try {
            databaseManager.open();

        List<Song> SongList = getListOfSongsWithAlbums(databaseManager);
        Cursor cursor = databaseManager.fetchAlbumsSongsFullInfo(id,"title");
        SongList.removeIf(s -> !s.getAlbum().equals(album_name));

        if(cursor.moveToFirst()){
            @SuppressLint("Range") int song_id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SONG_ID));
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_NAME));
            @SuppressLint("Range") String artist = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ARTIST));
            @SuppressLint("Range") String album = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_ALBUM));
            SongList.removeIf(s -> s.getAlbum().equals(album));
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

}
