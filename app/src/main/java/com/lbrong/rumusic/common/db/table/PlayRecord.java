package com.lbrong.rumusic.common.db.table;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

/**
 * @author lbRoNG
 * @since 2018/11/19
 */
public class PlayRecord extends LitePalSupport {
    @Column(unique = true)
    private long id;
    private long songId;
    private int repeat;  // 播放重复次数

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
}
