package com.lbrong.rumusic.common.event.home;

/**
 * @author lbRoNG
 * @since 2018/10/23
 */
public final class PageRefresh {
    private int index;

    public PageRefresh(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
