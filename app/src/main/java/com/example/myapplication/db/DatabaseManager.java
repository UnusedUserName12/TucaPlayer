package com.example.myapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.sql.SQLDataException;
import java.sql.SQLException;

public class DatabaseManager  {
    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;
    public DatabaseManager(Context ctx){
        this.context = ctx;
    }

    public DatabaseManager open() throws SQLDataException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        dbHelper.close();
    }

    public void insertPlaylist(String name, String picture_name){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.PLAYLIST_NAME, name);
        contentValues.put(DatabaseHelper.PLAYLIST_PICTURE, picture_name);
        database.insert(DatabaseHelper.PLAYLIST_TABLE,null,contentValues);
    }

    public void insertSong(String filename,String name,String album,String artist,String genre){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.SONG_FILENAME,filename);
        contentValues.put(DatabaseHelper.SONG_NAME,name);
        contentValues.put(DatabaseHelper.SONG_ALBUM,album);
        contentValues.put(DatabaseHelper.SONG_ARTIST,artist);
        contentValues.put(DatabaseHelper.SONG_GENRE,genre);
        database.insert(DatabaseHelper.SONG_TABLE,null,contentValues);
    }


    //WARNING - this will break?
    public void insertPlaylistSong(int playlist_id, int song_id){
        ContentValues contentValues = new ContentValues();

        contentValues.put(DatabaseHelper.PLAY_SONGS_TABLE_PLAYLIST_ID,playlist_id);
        contentValues.put(DatabaseHelper.PLAY_SONGS_TABLE_SONG_ID,song_id);
        database.insert(DatabaseHelper.PLAYLIST_SONGS_TABLE,null,contentValues);
    }

    public Cursor fetchPlaylists(){
        String [] columns = new String[] {DatabaseHelper.PLAYLIST_ID,DatabaseHelper.PLAYLIST_NAME,DatabaseHelper.PLAYLIST_PICTURE};
        Cursor cursor = database.query(DatabaseHelper.PLAYLIST_TABLE, columns,null,null,null,null,null,null);
        if (cursor !=null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getPlaylistById(int id){
        String [] columns = new String[] {DatabaseHelper.PLAYLIST_ID,DatabaseHelper.PLAYLIST_NAME,DatabaseHelper.PLAYLIST_PICTURE};
        Cursor cursor = database.query(DatabaseHelper.PLAYLIST_TABLE, columns,DatabaseHelper.PLAYLIST_ID+"="+id,null,null,null,null,null);
        if (cursor !=null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetchSongs(){
        String [] columns = new String[] {DatabaseHelper.SONG_ID,DatabaseHelper.SONG_FILENAME,DatabaseHelper.SONG_NAME,DatabaseHelper.SONG_ALBUM,DatabaseHelper.SONG_ARTIST,DatabaseHelper.SONG_GENRE};
        Cursor cursor = database.query(DatabaseHelper.SONG_TABLE, columns,null,null,null,null,null,null);
        if (cursor !=null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetchPlaylistSongs(){
        String [] columns = new String[] {DatabaseHelper.PLAY_SONGS_TABLE_PLAYLIST_ID,DatabaseHelper.PLAY_SONGS_TABLE_SONG_ID};
        Cursor cursor = database.query(DatabaseHelper.PLAYLIST_SONGS_TABLE, columns,null,null,null,null,null,null);
        if (cursor !=null){
            cursor.moveToFirst();
        }
        return cursor;
    }


    public void deletePlaylist(int id){
        database.delete(DatabaseHelper.PLAYLIST_TABLE, DatabaseHelper.PLAYLIST_ID + "="+id,null);
    }

    public void deleteSong(int id){
        database.delete(DatabaseHelper.SONG_TABLE, DatabaseHelper.SONG_ID + "="+id,null);
    }

    public void deletePlaylistSong(int playlist_id, int song_id){
        database.delete(DatabaseHelper.PLAYLIST_SONGS_TABLE,DatabaseHelper.PLAY_SONGS_TABLE_PLAYLIST_ID + " = "+playlist_id+" AND "+DatabaseHelper.PLAY_SONGS_TABLE_SONG_ID+"="+song_id,null);
    }

    /*
    **************DEBUG FUNCTIONS**************
     */

    public void dropAllTables(){
        database.execSQL("DROP TABLE IF EXISTS "+ DatabaseHelper.PLAYLIST_TABLE);
        database.execSQL("DROP TABLE IF EXISTS "+ DatabaseHelper.SONG_TABLE);
        database.execSQL("DROP TABLE IF EXISTS "+ DatabaseHelper.PLAYLIST_SONGS_TABLE);
    }

    public void createTables(){
        database.execSQL(DatabaseHelper.CREATE_SONG_TABLE_QUERY);
        database.execSQL(DatabaseHelper.CREATE_PLAYLIST_TABLE_QUERY);
        database.execSQL(DatabaseHelper.CREATE_PLAYLIST_SONGS_TABLE_QUERY);
    }


}
