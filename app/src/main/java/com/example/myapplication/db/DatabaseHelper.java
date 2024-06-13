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

    static final String CREATE_PLAYLIST_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS "+PLAYLIST_TABLE
            +"("
            + PLAYLIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PLAYLIST_NAME + " TEXT NOT NULL UNIQUE, "
            + PLAYLIST_PICTURE + " TEXT"
            + ");";


    public static final String SONG_TABLE = "SONGS";
    public static final String SONG_ID = "song_id";
    public static final String SONG_FILENAME = "song_filename";
    public static final String SONG_NAME = "song_name";
    public static final String SONG_ALBUM = "song_album";
    public static final String SONG_ARTIST = "song_artist";
    public static final String SONG_GENRE = "song_genre";
    public static final  String SONG_DURATION = "song_duration";

    static final String CREATE_SONG_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " + SONG_TABLE
            + "("
            + SONG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + SONG_FILENAME + " TEXT NOT NULL,"
            + SONG_NAME + " TEXT NOT NULL,"
            + SONG_ALBUM + " TEXT NOT NULL,"
            + SONG_ARTIST + " TEXT NOT NULL,"
            + SONG_GENRE + " TEXT NOT NULL,"
            + SONG_DURATION + " INTEGER NOT NULL"
            + ");";


    public static final String PLAYLIST_SONGS_TABLE = "PLAYLIST_SONGS";
    public static final String PLAY_SONGS_TABLE_PLAYLIST_ID = "playlist_id";
    public static final String PLAY_SONGS_TABLE_SONG_ID = "song_id";

    static final String CREATE_PLAYLIST_SONGS_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " + PLAYLIST_SONGS_TABLE
            +"("
            + PLAY_SONGS_TABLE_PLAYLIST_ID + " INTEGER, "
            + PLAY_SONGS_TABLE_SONG_ID + " INTEGER,"
            + "FOREIGN KEY ("+PLAY_SONGS_TABLE_PLAYLIST_ID+") REFERENCES " + PLAYLIST_TABLE + "(" + PLAYLIST_ID + ") ON DELETE CASCADE,"
            + "FOREIGN KEY ("+PLAY_SONGS_TABLE_SONG_ID+") REFERENCES " + SONG_TABLE + "(" + SONG_ID + ") ON DELETE CASCADE"
            + ");";

    public static final String ALBUM_TABLE = "ALBUMS";
    public static final String ALBUM_ID = "album_id";
    public static final String ALBUM_NAME = "album_name";

    static final String CREATE_ALBUM_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS "+ALBUM_TABLE
            +"("
            + ALBUM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ALBUM_NAME + " TEXT NOT NULL UNIQUE "
            + ");";

    public static final String ALBUM_SONGS_TABLE = "ALBUM_SONGS";
    public static final String ALBUM_SONG_TABLE_ALBUM_ID = "album_id";
    public static final String ALBUM_SONG_TABLE_SONG_ID = "song_id";

    static final String CREATE_ALBUM_SONGS_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " + ALBUM_SONGS_TABLE
            +"("
            + ALBUM_SONG_TABLE_ALBUM_ID + " INTEGER, "
            + ALBUM_SONG_TABLE_SONG_ID + " INTEGER,"
            + "FOREIGN KEY ("+ALBUM_SONG_TABLE_ALBUM_ID+") REFERENCES " + ALBUM_TABLE + "(" + ALBUM_ID + ") ON DELETE CASCADE,"
            + "FOREIGN KEY ("+ALBUM_SONG_TABLE_SONG_ID+") REFERENCES " + SONG_TABLE + "(" + SONG_ID + ") ON DELETE CASCADE"
            + ");";

    public static final String FAVORITES_TABLE = "FAVORITES";
    public static final String FAVORITE_ID = "song_id";

    static final String CREATE_FAVORITES_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " + FAVORITES_TABLE
            +"("
            + FAVORITE_ID + " INTEGER, "
            + "FOREIGN KEY ("+FAVORITE_ID+") REFERENCES " + SONG_TABLE + "(" + SONG_ID + ") ON DELETE CASCADE"
            + ");";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PLAYLIST_TABLE_QUERY);
        db.execSQL(CREATE_SONG_TABLE_QUERY);
        db.execSQL(CREATE_PLAYLIST_SONGS_TABLE_QUERY);
        db.execSQL(CREATE_ALBUM_TABLE_QUERY);
        db.execSQL(CREATE_ALBUM_SONGS_TABLE_QUERY);
        db.execSQL(CREATE_FAVORITES_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ PLAYLIST_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+ SONG_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+ PLAYLIST_SONGS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+ ALBUM_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+ ALBUM_SONGS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+ FAVORITES_TABLE);
    }
}
