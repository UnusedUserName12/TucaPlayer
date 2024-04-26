package com.example.myapplication.obj;

public class Song {

    private String filename;
    private String songName;
    private long duration;
    private String author;
    private int image;

    public Song() {
    }

    public Song(String filename) {
        this.filename = filename;
    }

    public Song(String filename, String songName, long duration, String author, int image) {
        this.filename = filename;
        this.songName = songName;
        this.duration = duration;
        this.author = author;
        this.image = image;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

}
