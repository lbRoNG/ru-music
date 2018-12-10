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

import com.lbrong.rumusic.common.db.table.PlayList;
import com.lbrong.rumusic.common.db.table.Song;
import com.lbrong.rumusic.common.event.music.MusicState;
import com.lbrong.rumusic.common.utils.MusicHelper;
import com.lbrong.rumusic.common.utils.SendEventUtils;
import com.lbrong.rumusic.common.utils.SettingHelper;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author lbRoNG
 * @since 2018/10/22
 * 音乐播放服务
 */
public class PlayService extends Service implements MediaPlayer.OnCompletionListener {
    private MediaPlayer mPlayer;
    // 正在播放的音乐
    private Song currentAudio;
    // 播放列表
    private PlayList playList;
    // 进度保存任务
    private Disposable recordTask;
    // 任务集合
    private CompositeDisposable compositeDisposable;
    // 记录间隔
    private final static int RECORD_INTERVAL = 5;

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
            compositeDisposable = new CompositeDisposable();
            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // 发送播放完成的通知
        SendEventUtils.post(MusicState.MUSIC_COMPLETE);
        // 查看配置是否需要自动播放
        if(SettingHelper.build().isAutoNext()){
            next(false);
        }
    }

    @Override
    public void onDestroy() {
        stop();
        mPlayer.release();
        mPlayer = null;
        compositeDisposable.clear();
        compositeDisposable = null;
        super.onDestroy();
    }

    /**
     * 设置播放地址
     */
    public void setAudio(@NonNull Song audio){
        try {
            // 设置实体
            currentAudio = audio;
            //　更新播放列表
            playList.setPlayingId(currentAudio.getSongId());
            playList.setRecord(1);
            playList.updateAsync(playList.getId());
            // 设置资源
            if(mPlayer != null && !TextUtils.isEmpty(currentAudio.getUrl())){
                try {
                    mPlayer.reset();
                    mPlayer.setDataSource(getApplicationContext()
                            ,Uri.fromFile(new File(currentAudio.getUrl())));
                } catch (IllegalStateException e){
                    playFail();
                    e.printStackTrace();
                }
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
            // 开始播放
            try {
                mPlayer.prepareAsync();
                mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        // 开启记录
                        startRecord();
                        // 准备完毕开始播放
                        mPlayer.start();
                        // 发送播放音乐的通知
                        SendEventUtils.post(MusicState.MUSIC_PLAY);
                    }
                });

                mPlayer.setOnCompletionListener(PlayService.this);
            } catch (IllegalStateException e){
                playFail();
                e.printStackTrace();
            }
        }
    }

    /**
     * 重新准备歌曲，准备完处于等待状态
     * 用于退出app后恢复播放
     */
    public void rePrepare(@NonNull Song audio,final int start){
        currentAudio = audio;
        if(mPlayer != null && !TextUtils.isEmpty(currentAudio.getUrl())){
            try {
                mPlayer.reset();
                mPlayer.setDataSource(getApplicationContext()
                        ,Uri.fromFile(new File(currentAudio.getUrl())));
                mPlayer.prepareAsync();
                mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mPlayer.start();
                        mPlayer.seekTo(start);
                        mPlayer.pause();
                    }
                });

                mPlayer.setOnCompletionListener(PlayService.this);
            } catch (Exception e){
                playFail();
                e.printStackTrace();
            }
        }
    }

    /**
     * 播放失败
     */
    public void playFail(){
        if(mPlayer != null){
            mPlayer.stop();
        }
        SendEventUtils.post(MusicState.MUSIC_FAIL);
    }

    /**
     * 重新播放
     */
    public void rePlay(){
        if(mPlayer != null){
            mPlayer.seekTo(0);
            SendEventUtils.post(MusicState.MUSIC_RE_PLAY);
        }
    }

    /**
     * 按指定进度播放
     */
    public void seekTo(int mesc){
        if(mPlayer != null){
            mPlayer.seekTo(mesc);
            SendEventUtils.post(MusicState.MUSIC_SEEK_TO);
        }
    }

    /**
     * 暂停播放
     */
    public void pause(){
        if(mPlayer != null && mPlayer.isPlaying()){
            mPlayer.pause();
            stopRecord();
            SendEventUtils.post(MusicState.MUSIC_PAUSE);
        }
    }

    /**
     * 继续播放
     */
    public void continuePlay(){
        if(mPlayer != null && !mPlayer.isPlaying()){
            mPlayer.start();
            startRecord();
            SendEventUtils.post(MusicState.MUSIC_CONTINUE_PLAY);
        }
    }

    /**
     * 停止播放
     */
    public void stop(){
        if(mPlayer != null && mPlayer.isPlaying()){
            mPlayer.stop();
            stopRecord();
            SendEventUtils.post(MusicState.MUSIC_STOP);
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
     * 是否正在播放
     */
    public boolean isPlaying(){
        return mPlayer != null && mPlayer.isPlaying();
    }

    /**
     * 下一曲
     * 根据当前播放列表
     */
    public void next(final boolean fromUser){
        // 停止原来的播放
        mPlayer.stop();
        // 获取下一首
        Song song = MusicHelper.build().next(playList,fromUser);
        if(song != null){
            if(song.getSongId() == 0){
                // 列表全部播放完成
                SendEventUtils.post(MusicState.MUSIC_ALL_COMPLETE);
            } else {
                // 播放开始
                currentAudio = song;
                setAudio(currentAudio);
                playAudio();
            }
        } else {
            // 播放异常
            SendEventUtils.post(MusicState.MUSIC_STOP);
        }
    }

    /**
     * 上一曲
     * 根据当前播放列表
     */
    public void previous(){
        // 停止原来的播放
        mPlayer.stop();
        // 获取上一首
        Song song = MusicHelper.build().previous(playList);
        if(song == null){
            // 播放异常
            SendEventUtils.post(MusicState.MUSIC_STOP);
        } else {
            // 播放开始
            currentAudio = song;
            setAudio(currentAudio);
            playAudio();
        }
    }

    /**
     * 获取当前播放的音乐
     */
    public Song getCurrentAudio(){
        return currentAudio;
    }

    /**
     * 获取播放列表
     */
    public PlayList getPlayList() {
        return playList;
    }

    /**
     * 设置播放列表
     */
    public void setPlayList(PlayList playList) {
        this.playList = playList;
    }

    /**
     * 记录播放进度
     */
    private void startRecord(){
        if(mPlayer != null){
            stopRecord();
            compositeDisposable.add(
                    recordTask = Observable.interval(0,RECORD_INTERVAL,TimeUnit.SECONDS)
                            .subscribeOn(Schedulers.io())
                            .subscribe(new Consumer<Long>() {
                                @Override
                                public void accept(Long aLong){
                                    if(playList == null){
                                        playList = LitePal.findFirst(PlayList.class);
                                    }
                                    playList.setPlayingId(currentAudio.getSongId());
                                    playList.setRecord(mPlayer.getCurrentPosition());
                                    playList.update(playList.getId());
                                }
                            })
            );
        }
    }

    /**
     * 停止记录
     */
    private void stopRecord(){
        if(recordTask != null && !recordTask.isDisposed()){
            recordTask.dispose();
        }
    }
}
