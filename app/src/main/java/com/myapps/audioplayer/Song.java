package com.myapps.audioplayer;

import android.graphics.Bitmap;

public class Song {
    private String title;
    private String subTitle;
    private String path;

    private Bitmap poster;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Song(String title, String subTitle, String path) {
        this.title = title;
        this.subTitle = subTitle;
        this.path = path;

    }

    public Song(String title, String subTitle, String path, Bitmap poster) {
        this.title = title;
        this.subTitle = subTitle;
        this.path = path;
        this.poster = poster;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public Bitmap getPoster() {
        return poster;
    }

    public void setPoster(Bitmap poster) {
        this.poster = poster;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }
}
