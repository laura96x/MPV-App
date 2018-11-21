package com.example.hara.learninguimusicapp.Music;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

public class Song implements Serializable{
    private long id;
    private double duration;
    private int min, sec;
    private String title,
            artist, album, albumImage;

    // constructors
    public Song(long id, double duration, String title, String artist, String album) {
        this.id = id;
        this.duration = duration;
        this.title = title;
        this.artist = artist;
        this.album = album;
        albumImage = "";
        calculateMinSec();
    }
    public Song(String title, String artist) {
        id = 0;
        duration = 0;
        this.title = title;
        this.artist = artist;
        this.album = "";
        min = 0;
        sec = 0;
        albumImage = "";

    }

    // use duration to get length of song in minutes and seconds
    public void calculateMinSec() {
        min = (int) Math.floor(duration / 60);
        sec = (int) Math.round(duration % 60);
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String name) {
        this.title = name;
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

    public double getDuration() {
        return duration;
    }
    public void setDuration(double duration) {
        this.duration = duration;
    }

    public int getMin() {
        return min;
    }
    public void setMin(int min) {
        this.min = min;
    }

    public int getSec() {
        return sec;
    }
    public void setSec(int sec) {
        this.sec = sec;
    }

    public String getAlbumImage() {
        return albumImage;
    }
    public void setAlbumImage(String albumImage) {
        this.albumImage = albumImage;
    }

    @Override
    public String toString() {
        return "\nSong{" +

                "title='" + title + '\'' +

                '}';
    }
}


