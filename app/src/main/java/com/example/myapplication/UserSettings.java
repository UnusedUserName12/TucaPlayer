package com.example.myapplication;
/**
 * The UserSettings class stores the user's settings for a music player application.
 * It includes information about the last song and playlist played,
 * whether a song is currently playing, and playback options like repeat and shuffle.
 */
public class UserSettings {

    public static final String SONG_PREFERENCES = "song_preferences";
    public static final String LAST_SONG_ID = "last_song_id";
    public static final String SONG_IS_PLAYING = "song_is_playing";
    public static final String LAST_PLAYLIST_ID = "last_playlist_id";
    public static final String SONG_IS_ON_REPEAT = "song_is_on_repeat";
    public static final String SONG_IS_ON_SHUFFLE = "song_is_on_shuffle";
    public static final String IS_ALBUM = "is_album";
    public static final String SONG_TIMESTAMP = "song_timestamp";
    private int last_song_id;
    private int last_playlist_id;
    private boolean song_is_playing;
    private boolean song_is_on_repeat;
    private boolean song_is_on_shuffle;

    private boolean is_album;
    private int song_timestamp;

    public int getLast_song_id() {
        return last_song_id;
    }

    public void setLast_song_id(int last_song_id) {
        this.last_song_id = last_song_id;
    }

    public boolean isSong_playing() {
        return song_is_playing;
    }

    public void setSong_playing(boolean song_is_playing) {
        this.song_is_playing = song_is_playing;
    }

    public int getLast_playlist_id() {
        return last_playlist_id;
    }

    public void setLast_playlist_id(int last_playlist_id) {
        this.last_playlist_id = last_playlist_id;
    }

    public boolean isSong_on_repeat() {
        return song_is_on_repeat;
    }

    public void setSong_is_on_repeat(boolean song_is_on_repeat) {
        this.song_is_on_repeat = song_is_on_repeat;
    }

    public boolean isSong_on_shuffle() {
        return song_is_on_shuffle;
    }

    public void setSong_is_on_shuffle(boolean song_is_on_shuffle) {
        this.song_is_on_shuffle = song_is_on_shuffle;
    }

    public boolean isAlbum() {
        return is_album;
    }

    public void setIsAlbum(boolean is_album) {
        this.is_album = is_album;
    }

    public int getSong_timestamp() {
        return song_timestamp;
    }

    public void setSong_timestamp(int song_timestamp) {
        this.song_timestamp = song_timestamp;
    }
}
