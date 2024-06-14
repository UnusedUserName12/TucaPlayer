package com.example.myapplication.obj;

public class Album {

    private int id;
    private String name;
    private String artist;
    private String image_path;

    public Album(int id, String name,String artist, String image) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.image_path = image;
    }

    public int getId(){return id;}
    public void setId(int id){this.id = id;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagePath() {
        return image_path;
    }

    public void setImagePath(String image_path) {
        this.image_path = image_path;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

}
