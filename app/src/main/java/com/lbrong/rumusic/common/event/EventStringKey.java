package com.lbrong.rumusic.common.event;

/**
 * @author lbRoNG
 * @since 2018/10/23
 */
public final class EventStringKey {
    public static class Home{
        public final static String PAGE_REFRESH = "page_refresh";
    }

    public static class Music{
        public final static String MUSIC_PLAY = "music_play";
        public final static String MUSIC_RE_PLAY = "music_re_play";
        public final static String MUSIC_CONTINUE_PLAY = "music_continue_play";
        public final static String MUSIC_PAUSE = "music_pause";
        public final static String MUSIC_STOP = "music_stop";
        public final static String MUSIC_SEEK_TO = "music_seek_to";
        public final static String MUSIC_COMPLETE= "music_complete";
        public final static String MUSIC_STATE= "music_state";
    }
}
