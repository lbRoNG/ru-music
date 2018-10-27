package com.lbrong.rumusic.common.db.table;

import android.graphics.Bitmap;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * @author lbRoNG
 * @since 2018/10/18
 */
public class Song extends LitePalSupport {
    @Column(unique = true)
    private long id;
    private long albumId;
    @Column(defaultValue = "未知")
    private String album;
    @Column(defaultValue = "未知")
    private String title;
    @Column(defaultValue = "群星")
    private String artist;
    private String url;
    private long size;
    private long duration;
    private int music;
    private int bitrate;
    @Column(ignore = true)
    private PlayController controller;
    @Column(ignore = true)
    private WeakReference<Bitmap> bitmap;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song)) return false;
        Song song = (Song) o;
        return getId() == song.getId() &&
                getAlbumId() == song.getAlbumId() &&
                getSize() == song.getSize() &&
                getDuration() == song.getDuration();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAlbumId(), getSize(), getDuration());
    }

    public class PlayController {
        private boolean playing;

        public boolean isPlaying() {
            return playing;
        }

        public void setPlaying(boolean playing) {
            this.playing = playing;
        }
    }

    public PlayController getController() {
        return controller == null ? controller = new PlayController() : controller;
    }

    public void setController(PlayController controller) {
        this.controller = controller;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

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

    public int getMusic() {
        return music;
    }

    public void setMusic(int music) {
        this.music = music;
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