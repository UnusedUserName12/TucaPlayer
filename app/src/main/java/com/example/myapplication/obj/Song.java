package com.example.myapplication.obj;

public class Song {
    private int id;
    private String filename;
    private String songName;
    private long duration;
    private String genre;
    private String artist;
    private String album;

    private boolean selected;

    private boolean favorite;


    public Song() {
    }

    public Song(String filename) {
        this.filename = filename;
    }
    public Song(int id,String filename) {
        this.id = id;
        this.filename = filename;
        this.selected = false;
        this.favorite = false;
    }

    public Song(int id,String filename, String songName, String artist, String album,String genre,long duration) {
        this.id = id;
        this.filename = filename;
        this.songName = songName;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.duration = duration;
        this.selected = false;
        this.favorite = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }
    public void setAlbum(String album) {
        this.album = album;
    }
    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

}
