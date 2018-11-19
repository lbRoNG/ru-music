package com.lbrong.rumusic.common.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.lbrong.rumusic.common.db.table.Song;
import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
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
                    int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));

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
                        Bitmap bitmap = getAlbumArt(url, 8);
                        if(ObjectHelper.requireNonNull(bitmap)){
                            m.setBitmap(new WeakReference<>(bitmap));
                        }
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
     * @param ratio 压缩比例
     */
    public @Nullable Bitmap getAlbumArt(String url, int ratio) {
        try {
            FileInputStream inputStream = new FileInputStream(new File(url).getAbsolutePath());
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(inputStream.getFD());
            byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();
            Bitmap bitmap= BitmapFactory.decodeByteArray(picture,0,picture.length);
            if (ratio != 0) {
                bitmap = ImageUtils.compressBitmap(bitmap, ratio);
            }
            return bitmap;
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

}
