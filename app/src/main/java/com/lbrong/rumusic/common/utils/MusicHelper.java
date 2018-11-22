package com.lbrong.rumusic.common.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.db.table.Song;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lbRoNG
 * @since 2018/10/18
 */
public final class MusicHelper {
    private static MusicHelper musicHelper;

    private MusicHelper() {}

    public static MusicHelper build() {
        if (musicHelper == null) {
            synchronized (MusicHelper.class) {
                if (musicHelper == null) {
                    musicHelper = new MusicHelper();
                }
            }
        }
        return musicHelper;
    }

    /**
     * 获取本地音乐
     */
    public List<Song> getLocalMusic(@NonNull Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        List<Song> songList = new ArrayList<>();
        if (ObjectHelper.requireNonNull(contentResolver)) {
            Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    , null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

            if (cursor == null) {
                return songList;
            }

            if (cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    Song m = new Song();
                    long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    long artistId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
                    long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
                    long addedDate = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));
                    long modifiedDate = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED));

                    if (isMusic != 0 && duration / (500 * 60) >= 1) {
                        m.setSongId(id);
                        m.setTitle(title);
                        m.setArtist(artist);
                        m.setArtistId(artistId);
                        m.setDuration(duration);
                        m.setSize(size);
                        m.setUrl(url);
                        m.setAlbum(album);
                        m.setAlbumId(albumId);
                        m.setBitrate(getBitrate(size * 8,duration));
                        m.setCover(getAlbumArt(url));
                        m.setAddedDate(addedDate);
                        m.setModifiedDate(modifiedDate);
                        m.setCoverRes(getRandomSongCoverResId());
                        songList.add(m);
                    }
                    cursor.moveToNext();
                }
                cursor.close();
                return songList;
            }
        }
        return songList;
    }

    /**
     * 获取专辑封面
     * @param url mp3地址
     */
    public @Nullable byte[] getAlbumArt(String url) {
        try {
            FileInputStream inputStream = new FileInputStream(new File(url).getAbsolutePath());
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(inputStream.getFD());
            return mediaMetadataRetriever.getEmbeddedPicture();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取码率
     * @param size 文件大小，bit
     * @param duration 文件时长，ms
     */
    private int getBitrate(long size,long duration){
        return (int) (size / duration);
    }

    /**
     * 整理最新添加的歌曲
     * @param all 全部歌曲
     */
    public List<Song> settleNewestAdd(@NonNull List<Song> all){
        List<Song> newest = new ArrayList<>();
        long nowDate = System.currentTimeMillis();
        for (int i = 0; i < all.size(); i++) {
            Song item = all.get(i);
            long addDate = item.getAddedDate() * 1000;
            boolean beforeWeek = nowDate - addDate > 86400000 * 7;
            // 如果最新的一曲都是一周前的，就取最大50首
            if(i == 0 && beforeWeek){
                int max = Math.max(all.size(),50);
                return all.subList(0,max);
            }

            // 如果遇到在一周前的，如果集合大于等于50，就直接返回
            if(beforeWeek && newest.size() >= 50){
                break;
            }

            // 其余的直接添加
            newest.add(item);
        }
        return newest;
    }

    /**
     * 随机歌曲封面
     */
    public @DrawableRes int getRandomSongCoverResId(){
        int[] res = new int[]{R.drawable.ic_song_default_cover_1,R.drawable.ic_song_default_cover_2,
                R.drawable.ic_song_default_cover_3,R.drawable.ic_song_default_cover_4,
                R.drawable.ic_song_default_cover_5,R.drawable.ic_song_default_cover_6,
                R.drawable.ic_song_default_cover_7,R.drawable.ic_song_default_cover_8,
                R.drawable.ic_song_default_cover_9,R.drawable.ic_song_default_cover_10,
                R.drawable.ic_song_default_cover_11,R.drawable.ic_song_default_cover_12,
                R.drawable.ic_song_default_cover_13,R.drawable.ic_song_default_cover_14,
                R.drawable.ic_song_default_cover_15,R.drawable.ic_song_default_cover_16,
                R.drawable.ic_song_default_cover_17,R.drawable.ic_song_default_cover_18};
        return res[ThreadLocalRandom.current().nextInt(0, 17 + 1)];
    }

    /**
     * 随机首页专辑封面
     */
    public @DrawableRes int getRandomHomeSongListCoverResId(){
        int[] res = new int[]{R.drawable.ic_home_songlist_default_cover_1,R.drawable.ic_home_songlist_default_cover_2,
                R.drawable.ic_home_songlist_default_cover_3,R.drawable.ic_home_songlist_default_cover_4};
        return res[ThreadLocalRandom.current().nextInt(0, 4 + 1)];
    }
}
