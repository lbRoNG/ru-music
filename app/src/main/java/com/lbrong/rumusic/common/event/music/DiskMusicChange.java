package com.lbrong.rumusic.common.event.music;

/**
 * 本地音乐发生改变
 */
public final class DiskMusicChange {
    private int addCount;
    private int removeCount;

    public DiskMusicChange(int addCount, int removeCount) {
        this.addCount = addCount;
        this.removeCount = removeCount;
    }

    public int getAddCount() {
        return addCount;
    }

    public int getRemoveCount() {
        return removeCount;
    }
}
