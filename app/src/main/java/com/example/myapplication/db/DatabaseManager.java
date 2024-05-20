package com.example.myapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.sql.SQLDataException;

public class DatabaseManager  {
    private DatabaseHelper dbHelper;
    private final Context context;
    private SQLiteDatabase database;
    public DatabaseManager(Context ctx){
        this.context = ctx;
    }

    public void open() throws SQLDataException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
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

    public void insertSong(String filename,String name,String album,String artist,String genre,long duration){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.SONG_FILENAME,filename);
        contentValues.put(DatabaseHelper.SONG_NAME,name);
        contentValues.put(DatabaseHelper.SONG_ALBUM,album);
        contentValues.put(DatabaseHelper.SONG_ARTIST,artist);
        contentValues.put(DatabaseHelper.SONG_GENRE,genre);
        contentValues.put(DatabaseHelper.SONG_DURATION,duration);
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

    /**
     * Retrieves songs from the database based on the specified order option.
     *
     * @param orderOption the option to order the songs by. Valid options are "title", "album", "artist", "genre", or "duration".
     * @return a Cursor object containing the result set of the query. Returns null if an error occurs.
     */
    public Cursor fetchSongs(String orderOption){
        String [] columns = new String[] {DatabaseHelper.SONG_ID,DatabaseHelper.SONG_FILENAME,DatabaseHelper.SONG_NAME,DatabaseHelper.SONG_ALBUM,DatabaseHelper.SONG_ARTIST,DatabaseHelper.SONG_GENRE,DatabaseHelper.SONG_DURATION};
        Cursor cursor;
        orderOption = orderOption.toLowerCase();
        switch (orderOption){
            case "title":
                cursor = database.query(DatabaseHelper.SONG_TABLE, columns,null,null,null,null,DatabaseHelper.SONG_NAME,null);
                break;
            case "album":
                cursor = database.query(DatabaseHelper.SONG_TABLE, columns,null,null,null,null,DatabaseHelper.SONG_ALBUM,null);
                break;
            case "artist":
                cursor = database.query(DatabaseHelper.SONG_TABLE, columns,null,null,null,null,DatabaseHelper.SONG_ARTIST,null);
                break;
            case "genre":
                cursor = database.query(DatabaseHelper.SONG_TABLE, columns,null,null,null,null,DatabaseHelper.SONG_GENRE,null);
                break;
            case "duration":
                cursor = database.query(DatabaseHelper.SONG_TABLE, columns,null,null,null,null,DatabaseHelper.SONG_DURATION,null);
                break;
            default:
                cursor = database.query(DatabaseHelper.SONG_TABLE, columns,null,null,null,null,DatabaseHelper.SONG_NAME,null);
                break;
        }

        if (cursor !=null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetchPlaylistSongs(int id){
        String [] columns = new String[] {DatabaseHelper.PLAY_SONGS_TABLE_PLAYLIST_ID,DatabaseHelper.PLAY_SONGS_TABLE_SONG_ID};
        Cursor cursor = database.query(DatabaseHelper.PLAYLIST_SONGS_TABLE, columns,DatabaseHelper.PLAY_SONGS_TABLE_PLAYLIST_ID+"="+id,null,null,null,null,null);
        if (cursor !=null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    /**
     * Fetches detailed information about the songs in a specific playlist, ordered by the specified criteria.
     *
     * @param id the ID of the playlist for which to fetch song details.
     * @param orderOption the field by which to order the results. Can be one of "title", "album", "artist", "genre", or "duration".
     *                    If the provided value is not one of these, the results will be ordered by the song title by default.
     * @return a Cursor object positioned at the first entry of the result set, containing the detailed song information.
     *         The columns in the result set are:
     *         - {@link DatabaseHelper#PLAY_SONGS_TABLE_SONG_ID}
     *         - {@link DatabaseHelper#SONG_FILENAME}
     *         - {@link DatabaseHelper#SONG_NAME}
     *         - {@link DatabaseHelper#SONG_ALBUM}
     *         - {@link DatabaseHelper#SONG_ARTIST}
     *         - {@link DatabaseHelper#SONG_GENRE}
     *         - {@link DatabaseHelper#SONG_DURATION}
     */
    public Cursor fetchPlaylistSongsFullInfo(int id, String orderOption){
        String sql = "Select "
                + "l."+ DatabaseHelper.PLAY_SONGS_TABLE_SONG_ID + ","
                + "r." + DatabaseHelper.SONG_FILENAME + ","
                + "r." + DatabaseHelper.SONG_NAME + ","
                + "r." + DatabaseHelper.SONG_ALBUM + ","
                + "r." + DatabaseHelper.SONG_ARTIST + ","
                + "r." + DatabaseHelper.SONG_GENRE + ","
                + "r." + DatabaseHelper.SONG_DURATION
                + " FROM "
                + DatabaseHelper.PLAYLIST_SONGS_TABLE + " l "
                + " INNER JOIN "
                + DatabaseHelper.SONG_TABLE + " r "
                + " ON "
                + "r." + DatabaseHelper.SONG_ID + " = " + "l." + DatabaseHelper.PLAY_SONGS_TABLE_SONG_ID
                + " WHERE "
                + DatabaseHelper.PLAY_SONGS_TABLE_PLAYLIST_ID + " = " + id
                + " GROUP BY ";


        orderOption = orderOption.toLowerCase();
        switch (orderOption){
            case "title":
                sql += "r."+DatabaseHelper.SONG_NAME;
                break;
            case "album":
                sql += "r."+DatabaseHelper.SONG_ALBUM;
                break;
            case "artist":
                sql += "r."+DatabaseHelper.SONG_ARTIST;
                break;
            case "genre":
                sql += "r."+DatabaseHelper.SONG_GENRE;
                break;
            case "duration":
                sql += "r."+DatabaseHelper.SONG_DURATION;
                break;
            default:
                sql += "r."+DatabaseHelper.SONG_NAME;
                break;
        }
        Cursor cursor = database.rawQuery(sql,null);
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

    public void updatePlaylist(int playlist_id, String playlist_name, String playlist_image_path){

        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.PLAYLIST_NAME,playlist_name);
        values.put(DatabaseHelper.PLAYLIST_PICTURE,playlist_image_path);


        database.update(DatabaseHelper.PLAYLIST_TABLE,values,DatabaseHelper.PLAYLIST_ID+" = ? ",new String[]{String.valueOf(playlist_id)});

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
