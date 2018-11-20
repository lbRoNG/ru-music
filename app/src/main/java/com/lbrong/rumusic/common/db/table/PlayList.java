package com.lbrong.rumusic.common.db.table;

import org.litepal.LitePal;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;
import java.util.List;

/**
 * @author lbRoNG
 * @since 2018/11/19
 * 播放列表
 */
public class PlayList extends LitePalSupport {
    @Column(unique = true)
    private long id;
    @Column(defaultValue = "播放列表")
    private String name;
    private int count;   // 总数
    private long playingId; // 正在播放的音乐
    private long record; // 历史播放进度
    private List<Song> songs; // 音乐列表
    private List<PlayRecord> playRecords; // 列表内音乐播放的控制

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getPlayingId() {
        return playingId;
    }

    public void setPlayingId(long playingId) {
        this.playingId = playingId;
    }

    public long getRecord() {
        return record;
    }

    public void setRecord(long record) {
        this.record = record;
    }

    public List<Song> getSongs() {
        return songs = LitePal.where("playlist_id=" + this.id).find(Song.class);
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public List<PlayRecord> getPlayRecords() {
        return playRecords;
    }

    public void setPlayRecords(List<PlayRecord> playRecords) {
        this.playRecords = playRecords;
    }
}
