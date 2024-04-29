package com.example.myapplication.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "TUCAPLAYER.db";
    static int DATABASE_VERSION = 1;

    public static final String PLAYLIST_TABLE = "PLAYLISTS";
    public static final String PLAYLIST_ID = "playlist_id";
    public static final String PLAYLIST_NAME = "playlist_name";
    public static final String PLAYLIST_PICTURE = "playlist_picture";

    private static final String CREATE_PLAYLIST_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS "+PLAYLIST_TABLE
            +"("
            + PLAYLIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PLAYLIST_NAME + " TEXT NOT NULL, "
            + PLAYLIST_PICTURE + " TEXT"
            + ");";


    public static final String SONG_TABLE = "SONGS";
    public static final String SONG_ID = "song_id";
    public static final String SONG_FILENAME = "song_filename";

    private static final String CREATE_SONG_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " + SONG_TABLE
            + "("
            + SONG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + SONG_FILENAME + " TEXT NOT NULL"
            + ");";


    public static final String PLAYLIST_SONGS_TABLE = "PLAYLIST_SONGS";
    public static final String PLAY_SONGS_TABLE_PLAYLIST_ID = "playlist_id";
    public static final String PLAY_SONGS_TABLE_SONG_ID = "song_id";

    private static final String CREATE_PLAYLIST_SONGS_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " + PLAYLIST_SONGS_TABLE
            +"("
            + PLAY_SONGS_TABLE_PLAYLIST_ID + " INTEGER, "
            + PLAY_SONGS_TABLE_SONG_ID + " INTEGER,"
            + "FOREIGN KEY ("+PLAY_SONGS_TABLE_PLAYLIST_ID+") REFERENCES " + PLAYLIST_TABLE + "(" + PLAYLIST_ID + ") ON DELETE CASCADE,"
            + "FOREIGN KEY ("+PLAY_SONGS_TABLE_SONG_ID+") REFERENCES " + SONG_TABLE + "(" + SONG_ID + ") ON DELETE CASCADE"
            + ");";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PLAYLIST_TABLE_QUERY);
        db.execSQL(CREATE_SONG_TABLE_QUERY);
        db.execSQL(CREATE_PLAYLIST_SONGS_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ PLAYLIST_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+ SONG_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+ PLAYLIST_SONGS_TABLE);
    }
}
