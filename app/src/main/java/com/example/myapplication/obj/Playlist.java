package com.example.myapplication.obj;

import java.util.List;

//TODO: Check if actually useful
public class Playlist {
    private int id;
    private String name;
    private String image_path;

    public Playlist(int id, String name, String image) {
        this.id = id;
        this.name = name;
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

}
