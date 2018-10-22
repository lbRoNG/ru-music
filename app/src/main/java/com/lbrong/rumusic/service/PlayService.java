package com.lbrong.rumusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.lbrong.rumusic.bean.Song;
import java.io.File;
import java.io.IOException;

/**
 * @author lbRoNG
 * @since 2018/10/22
 * 音乐播放服务
 */
public class PlayService extends Service {
    private MediaPlayer mPlayer;
    private Song currentAudio;

    public class PlayBinder extends Binder {
        public PlayService getService(){
            return PlayService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(mPlayer == null){
            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
        super.onDestroy();
    }

    /**
     * 设置播放地址
     */
    public void setAudio(@NonNull Song audio){
        try {
            currentAudio = audio;

            if(mPlayer != null && !TextUtils.isEmpty(currentAudio.getUrl())){
                mPlayer.reset();
                mPlayer.setDataSource(getApplicationContext()
                        ,Uri.fromFile(new File(currentAudio.getUrl())));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始播放
     */
    public void playAudio(){
        if(currentAudio != null && !TextUtils.isEmpty(currentAudio.getUrl())
                && mPlayer != null){
            try {
                mPlayer.prepareAsync();
                mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mPlayer.start();
                    }
                });
            } catch (IllegalStateException e){
                playFail();
                e.printStackTrace();
            }
        }
    }

    /**
     * 播放失败
     */
    public void playFail(){

    }

    /**
     * 重新播放
     */
    public void rePlay(){
        if(mPlayer != null){
            if(mPlayer.isPlaying()){
                seekTo(0);
            } else {
                mPlayer.start();
            }
        }
    }

    /**
     * 按指定进度播放
     */
    public void seekTo(int mesc){
        if(mPlayer != null){
            mPlayer.seekTo(mesc);
        }
    }

    /**
     * 暂停播放
     */
    public void pause(){
        if(mPlayer != null && mPlayer.isPlaying()){
            mPlayer.pause();
        }
    }

    /**
     * 停止播放
     */
    public void stop(){
        if(mPlayer != null && mPlayer.isPlaying()){
            mPlayer.stop();
        }
    }

    /**
     * 获取当前播放的歌曲总进度
     */
    public int getDuration(){
        if(mPlayer != null){
            return mPlayer.getDuration();
        }
        return 0;
    }

    /**
     * 获取当前播放的歌曲的进度
     */
    public int getCurrentPosition(){
        if(mPlayer != null){
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * 获取当前播放的音乐
     */
    public Song getCurrentAudio(){
        return currentAudio;
    }

}
