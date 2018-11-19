package com.lbrong.rumusic.common.db.table;

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
    private int playing; // 正在播放的音乐位置
    private long record; // 历史播放进度
    private List<Song> songs;
    private List<PlayRecord> playRecords;

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

    public int getPlaying() {
        return playing;
    }

    public void setPlaying(int playing) {
        this.playing = playing;
    }

    public long getRecord() {
        return record;
    }

    public void setRecord(long record) {
        this.record = record;
    }

    public List<Song> getSongs() {
        return songs;
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
