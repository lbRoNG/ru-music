package com.lbrong.rumusic.bean;

import android.graphics.Bitmap;

import java.lang.ref.WeakReference;

/**
 * @author lbRoNG
 * @since 2018/10/18
 */
public class Song {
    private long id;
    private long albumId;
    private String title;
    private String artist;
    private long size;
    private String url;
    private int isMusic;
    private long duration;
    private String album;
    private WeakReference<Bitmap> bitmap;

    public WeakReference<Bitmap> getBitmap() {
        return bitmap;
    }

    public void setBitmap(WeakReference<Bitmap> bitmap) {
        this.bitmap = bitmap;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getIsMusic() {
        return isMusic;
    }

    public void setIsMusic(int isMusic) {
        this.isMusic = isMusic;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }
}
