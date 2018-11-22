package com.lbrong.rumusic.common.db.table;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;
import java.util.Objects;

/**
 * @author lbRoNG
 * @since 2018/10/18
 * 单曲
 */
public class Song extends LitePalSupport {
    @Column(unique = true)
    private long id;
    private long songId;
    private long albumId;
    private long artistId;
    @Column(defaultValue = "未知")
    private String album;
    @Column(defaultValue = "未知")
    private String title;
    @Column(defaultValue = "群星")
    private String artist;
    private String url;
    private String coverUrl;
    private int coverRes;
    private long size;
    private long duration;
    private int music;
    private int bitrate;
    private byte[] cover;
    private long addedDate;
    private long modifiedDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song)) return false;
        Song song = (Song) o;
        return getSongId() == song.getSongId() &&
                getAlbumId() == song.getAlbumId() &&
                getSize() == song.getSize() &&
                getDuration() == song.getDuration();
    }

    @Override
    public String toString() {
        return "Song{" +
                "songId=" + songId +
                ", albumId=" + albumId +
                ", artistId=" + artistId +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getSongId(), getAlbumId(), getSize(), getDuration());
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public byte[] getCover() {
        return cover;
    }

    public void setCover(byte[] cover) {
        this.cover = cover;
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
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

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public long getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(long addedDate) {
        this.addedDate = addedDate;
    }

    public long getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(long modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public int getCoverRes() {
        return coverRes;
    }

    public void setCoverRes(int coverRes) {
        this.coverRes = coverRes;
    }
}
