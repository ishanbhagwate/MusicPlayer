package com.shivam.musicplayer.Model;

import android.net.Uri;

public class Songs {

    private String title,artist, data,album;
    private Uri songUri;


    public Songs(String title, String artist, String data, String album, Uri songUri) {
        this.title = title;
        this.artist = artist;
        this.data = data;
        this.album = album;
        this.songUri = songUri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public Uri getSongUri() {
        return songUri;
    }

    public void setSongUri(Uri songUri) {
        this.songUri = songUri;
    }


}
