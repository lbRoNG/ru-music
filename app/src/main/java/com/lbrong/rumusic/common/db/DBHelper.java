package com.lbrong.rumusic.common.db;

import android.content.ContentValues;

import com.lbrong.rumusic.common.db.table.PlaySong;
import com.lbrong.rumusic.common.db.table.Song;

import org.litepal.LitePal;

/**
 * 数据库管理
 */
public class DBHelper {
    private static DBHelper dbHelper;

    private DBHelper() {}

    public static DBHelper build() {
        if (dbHelper == null) {
            synchronized (DBHelper.class) {
                if (dbHelper == null) {
                    dbHelper = new DBHelper();
                }
            }
        }
        return dbHelper;
    }

    /**
     * 通过songId查询Song
     */
    public Song querySongBySongId(long songId){
        return LitePal.where("songid=" + songId).findFirst(Song.class);
    }

    /**
     * 通过songId删除Song
     */
    public void removeSongBySongId(long songId){
        LitePal.deleteAll(Song.class,"songid=" + songId);
    }

    /**
     * 重置播放列表中的播放状态
     */
    public void resetPlayingState(){
        ContentValues contentValues = new ContentValues();
        contentValues.put("state",0);
        LitePal.updateAll(PlaySong.class,contentValues,"state=1");
    }

    /**
     * 设置歌曲在播放列表中的播放状态
     */
    public void setPlayingState(long songId){
        ContentValues contentValues = new ContentValues();
        contentValues.put("state",1);
        LitePal.updateAll(PlaySong.class,contentValues,"songid="+songId);
    }

    /**
     * 查询正在播放的歌曲具体信息
     */
    public Song queryPlayingSong(){
        PlaySong playSong = queryPlayingSongAtPlayList();
        return LitePal.where("songid=" + playSong.getSongId()).findFirst(Song.class);
    }

    /**
     * 查询正在播放的歌曲具体信息
     */
    public PlaySong queryPlayingSongAtPlayList(){
        return LitePal.where("state=1").findFirst(PlaySong.class);
    }

    /**
     * 查询正在播放的歌曲具体信息
     */
    public PlaySong queryPlayingSongById(long songId){
        return LitePal.where("songid=" + songId).findFirst(PlaySong.class);
    }
}
