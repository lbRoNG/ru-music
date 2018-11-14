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
import com.lbrong.rumusic.common.type.PlayMethodEnum;

import org.litepal.LitePal;

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
            //e.printStackTrace();
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
     * 根据播放方法和播放列表计算出下一首要播放的歌曲
     * @param ids 播放列表ids
     * @param randomIds 播放列表随机ids
     * @param currentId 当前播放的id
     * @param fromUser 是否是用户主动切换
     */
    public @Nullable Song getNext(List<Long> ids, List<Long> randomIds, long currentId, boolean fromUser){
        if(ObjectHelper.requireNonNull(ids)
                || ObjectHelper.requireNonNull(randomIds)){
            // 播放方式
            PlayMethodEnum method = SettingHelper.build().getPlayMethod();
            if(method == PlayMethodEnum.RANDOM){
                ids = randomIds;
            }
            // 列表长度
            int size = ids.size();
            // 在列表中的位置
            int index = ids.indexOf(currentId);
            // 下一首的位置
            int next = index + 1;
            switch (method){
                case ORDER:
                    if(!fromUser){
                        // 已经是最后一首
                        if(size == next){
                            // 返回内容为空的实体
                            // 标识播放全部结束
                            return new Song();
                        }
                        break;
                    }
                case ORDER_LOOP:
                case RANDOM:
                    // 随机播放依赖播放列表，下一曲的逻辑和顺序循环一样
                    // 最后一首就从头开始
                    if(size == next){
                        next = 0;
                    }
                    break;
                case SINGLE:
                    if(!fromUser){
                        // 单曲循环
                        next = index;
                    }
                    break;
            }
            // 获取到id
            long nextId = ids.get(next);
            // 数据库找寻对应的歌曲
            return LitePal.find(Song.class, nextId);
        }
       return null;
    }

    /**
     * 根据播放方法和播放列表计算出上一首要播放的歌曲
     * 触发上一曲肯定是用户主动切换
     * @param ids 播放列表ids
     * @param randomIds 播放列表随机ids
     * @param currentId 当前播放的id
     */
    public @Nullable Song getPrevious(List<Long> ids, List<Long> randomIds, long currentId){
        if(ObjectHelper.requireNonNull(ids)
                || ObjectHelper.requireNonNull(randomIds)){
            // 播放方式
            PlayMethodEnum method = SettingHelper.build().getPlayMethod();
            if(method == PlayMethodEnum.RANDOM){
                ids = randomIds;
            }
            // 列表长度
            int size = ids.size();
            // 在列表中的位置
            int index = ids.indexOf(currentId);
            // 下一首的位置
            int previous = index - 1;
            // 随机播放依赖播放列表，下一曲的逻辑和顺序循环一样
            // 最后一首就从头开始
            if(previous == -1){
                previous = size - 1;
            }
            // 获取到id
            long previousId = ids.get(previous);
            // 数据库找寻对应的歌曲
            return LitePal.find(Song.class, previousId);
        }
        return null;
    }

    /**
     * 播放歌曲时同步歌曲在数据库种的状态
     */
    public void setSongPlayingState(Song currentAudio){
        // 复位之前播放歌曲的状态
        Song last = LitePal.where("state=1").findFirst(Song.class);
        if(last != null){
            last.setPlaying(false);
            last.save();
        }
        // 更新状态，并同步到数据库
        currentAudio.setPlaying(true);
        currentAudio.update(currentAudio.getId());
    }
}
