package com.lbrong.rumusic.common.db.table;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.Objects;

/**
 * @author lbRoNG
 * @since 2018/11/16
 * 播放列表中的歌曲
 */
public class PlaySong extends LitePalSupport {
    @Column(unique = true)
    private long id;
    private long songId; // 对应的歌曲id
    private int repeat;  // 歌曲要循环的次数
    private long record ;// 歌曲播放记录
    private int state ;  // 歌曲状态

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaySong playSong = (PlaySong) o;
        return id == playSong.id &&
                songId == playSong.songId;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, songId);
    }

    public PlaySong(long songId) {
        this.songId = songId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public long getRecord() {
        return record;
    }

    public void setRecord(long record) {
        this.record = record;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
