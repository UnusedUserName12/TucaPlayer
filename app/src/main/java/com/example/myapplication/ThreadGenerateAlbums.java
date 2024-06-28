package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import com.example.myapplication.db.DatabaseHelper;
import com.example.myapplication.db.DatabaseManager;
import com.example.myapplication.interfaces.ThreadGenerateAlbumsListener;
import com.example.myapplication.obj.Album;
import com.example.myapplication.obj.Song;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThreadGenerateAlbums extends Thread{
    private final MainActivity mainActivity;

    public static void setThreadGenerateAlbumsListener(ThreadGenerateAlbumsListener listener) {
        threadGenerateAlbumsListener = listener;
    }

    private static ThreadGenerateAlbumsListener threadGenerateAlbumsListener;

    public static boolean isFinished;

    ThreadGenerateAlbums(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        isFinished=false;
    }
    @Override
    public void run() {
        super.run();
        updateAlbums();
    }

    private void updateAlbums() {
        DatabaseManager databaseManager = new DatabaseManager(mainActivity);
        try {
            databaseManager.open();
            List<Song> songList = getListOfSongsWithAlbums(databaseManager);
            List<Album> albumList = getListOfAlbums(databaseManager);

            Set<String> existingAlbums = new HashSet<>();
            for (Album album : albumList) {
                existingAlbums.add(album.getName());
            }

            Set<String> songAlbums = new HashSet<>();
            for (Song song : songList) {
                songAlbums.add(song.getAlbum());
            }

            List<Album> albumsToAdd = new ArrayList<>();
            for (String albumName : songAlbums) {
                if (!existingAlbums.contains(albumName)) {
                    Song exampleSong = songList.stream()
                            .filter(s -> s.getAlbum().equals(albumName))
                            .findFirst()
                            .orElse(null);
                    if (exampleSong != null) {
                        albumsToAdd.add(new Album(0, albumName, exampleSong.getArtist(), null));
                    }
                }
            }

            if (!albumsToAdd.isEmpty()) {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                if (mainActivity != null) {
                    for (Album album : albumsToAdd) {
                        for (Song song : songList) {
                            if (song.getAlbum().equals(album.getName())) {
                                File songFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), song.getFilename());
                                if (songFile.exists()) {
                                    mmr.setDataSource(songFile.getPath());
                                    byte[] data = mmr.getEmbeddedPicture();
                                    if (data != null) {
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        mainActivity.saveToInternalStorage(bitmap, song.getAlbum());

                                        Bitmap blurBitmap = blur(mainActivity, bitmap);
                                        mainActivity.saveToInternalStorage(blurBitmap, song.getAlbum()+"_blur");

                                        if (!existingAlbums.contains(song.getAlbum())) {
                                            databaseManager.insertAlbum(song.getAlbum(), song.getArtist(), song.getAlbum());
                                            existingAlbums.add(song.getAlbum());  // Update the set to avoid duplicates
                                        }
                                    } else {
                                        if (!existingAlbums.contains(song.getAlbum())) {
                                            databaseManager.insertAlbum(song.getAlbum(), song.getArtist(), "placeholder.png");
                                            existingAlbums.add(song.getAlbum());  // Update the set to avoid duplicates
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }



        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            databaseManager.close();
            if(threadGenerateAlbumsListener !=null){
                new Handler(Looper.getMainLooper()).post(() -> {
                    threadGenerateAlbumsListener.OnThreadFinish();
                });
            }
            isFinished=true;
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

    private static Bitmap blur(Context context, Bitmap image) {
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
}
