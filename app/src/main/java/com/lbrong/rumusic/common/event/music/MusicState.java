package com.lbrong.rumusic.common.event.music;

public enum MusicState {
    // 开始播放
    MUSIC_PLAY,
    // 重新播放
    MUSIC_RE_PLAY,
    // 继续播放
    MUSIC_CONTINUE_PLAY,
    // 暂停
    MUSIC_PAUSE,
    // 停止
    MUSIC_STOP,
    // 进度改变
    MUSIC_SEEK_TO,
    // 失败
    MUSIC_FAIL,
    // 单曲播放完成
    MUSIC_COMPLETE,
    // 全部播放完成
    MUSIC_ALL_COMPLETE
}
