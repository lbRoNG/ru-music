package com.lbrong.rumusic.presenter.play;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.adapter.PlayPlayListSongAdapter;
import com.lbrong.rumusic.common.db.table.PlayList;
import com.lbrong.rumusic.common.db.table.Song;
import com.lbrong.rumusic.common.event.music.MusicState;
import com.lbrong.rumusic.common.type.PlayMethodEnum;
import com.lbrong.rumusic.common.utils.SendEventUtils;
import com.lbrong.rumusic.common.utils.SettingHelper;
import com.lbrong.rumusic.iface.listener.OnPlayMethodChangeListener;
import com.lbrong.rumusic.presenter.base.ActivityPresenter;
import com.lbrong.rumusic.service.PlayService;
import com.lbrong.rumusic.view.play.PlayDelegate;
import org.greenrobot.eventbus.Subscribe;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class PlayActivity
       extends ActivityPresenter<PlayDelegate>
       implements GestureDetector.OnGestureListener,OnPlayMethodChangeListener,View.OnClickListener
        ,SeekBar.OnSeekBarChangeListener {

    // 播放服务
    private PlayService playService;
    // 连接引用
    private ServiceConnection serviceConnection;
    // 手势监听
    private GestureDetector gestureDetector;
    // 是否列表播放完成
    private boolean isMusicAllComplete;
    // 进度条计时器
    private Disposable progressTimer;
    // 播放列表
    private PlayPlayListSongAdapter playListSongAdapter;

    @Override
    protected Class<PlayDelegate> getDelegateClass() {
        return PlayDelegate.class;
    }

    @Override
    protected boolean displayHomeAsUp() {
        return false;
    }

    @Override
    protected void setting() {
        super.setting();
        // 动画效果
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        Transition anim = TransitionInflater.from(this).inflateTransition(R.transition.play_detail_explode);
        getWindow().setExitTransition(anim);
        getWindow().setEnterTransition(anim);
        getWindow().setReenterTransition(anim);
    }

    @Override
    protected void onDestroy() {
        // 解绑事件
        SendEventUtils.unregister(this);
        // 解绑服务
        if(serviceConnection != null && playService != null){
            unbindService(serviceConnection);
        }
        // 停止计时
        stopTimer();
        super.onDestroy();
    }

    @Override
    protected void init() {
        super.init();
        // 手势监听
        gestureDetector = new GestureDetector(this,this);
        // 事件接收
        SendEventUtils.register(this);
        // 绑定服务
        bindPlayService();
    }

    @Override
    protected void bindEvenListener() {
        super.bindEvenListener();
        viewDelegate.setOnPlayMethodChangeListener(this);
        viewDelegate.setSeekBarListener(this);
        viewDelegate.setOnClickListener(this,R.id.iv_play,R.id.iv_previous,R.id.iv_next);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(MusicState state){
        switch (state){
            // 新歌曲播放
            case MUSIC_PLAY:
                settingUI();
            // 重新播放
            case MUSIC_RE_PLAY:
            // 继续
            case MUSIC_CONTINUE_PLAY:
                viewDelegate.startCDRotateAnim();
            case MUSIC_SEEK_TO:
                startTimer();
                viewDelegate.setPlayIcon(true);
                break;
            // 全部播放完成
            case MUSIC_ALL_COMPLETE:
                // 播放列表播放完毕，并且不循环
                isMusicAllComplete = true;
            case MUSIC_PAUSE:
            case MUSIC_STOP:
            case MUSIC_COMPLETE:
                // 计时器
                stopTimer();
                // 停止
                viewDelegate.setPlayIcon(false);
                // cd动画
                viewDelegate.stopCDRotateAnim();
                break;
        }
    }

    @Override
    public void onPlayMethodChange(PlayMethodEnum method) {
        SettingHelper.build().playMethod(method);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser && playService != null){
            playService.seekTo(progress * 1000);
            playService.continuePlay();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_play:
                if(playService != null){
                    if(playService.isPlaying()){
                        // 正在播放状态就暂停
                        playService.pause();
                    } else {
                        if(isMusicAllComplete){
                            // 如果列表完成了一次，要重新设置起始状态才能开始
                            isMusicAllComplete = false;
                            playService.setAudio(playService.getCurrentAudio());
                            playService.playAudio();
                        } else {
                            // 暂停状态直接继续播放
                            playService.continuePlay();
                        }
                    }
                }
                break;
            case R.id.iv_previous:
                if(playService != null){
                    playService.previous();
                }
                break;
            case R.id.iv_next:
                if(playService != null){
                    playService.next(true);
                }
                break;
        }
    }

    /**
     * 绑定服务
     */
    private void bindPlayService(){
        if (playService == null) {
            Intent intent = new Intent(this, PlayService.class);
            bindService(intent, serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    // 拿到服务引用
                    PlayService.PlayBinder binder = (PlayService.PlayBinder) service;
                    playService = binder.getService();
                    if(playService != null){
                        // 开始设置
                        settingUI();
                        // 设置播放列表
                        settingPlayList();
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    playService = null;
                }
            }, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * 设置UI
     */
    private void settingUI(){
        Song playing = playService.getCurrentAudio();
        // 歌曲信息
        viewDelegate.setMusicInfo(playing.getCover(),playing.getTitle(),playing.getArtist(),playService.isPlaying());
        // 进度条信息
        viewDelegate.setMusicDuration(playing.getDuration(),playService.getCurrentPosition());
        // 播放方式
        viewDelegate.setPlayMethod(SettingHelper.build().getPlayMethod());
    }

    /**
     * 设置播放列表
     * 播放详情不会更新播放列表，直接到服务取出就好
     */
    private void settingPlayList() {
        if (playService != null) {
            PlayList playList = playService.getPlayList();
            playListSongAdapter = new PlayPlayListSongAdapter(playList);
            viewDelegate.setPlayListSongsAdapter(playListSongAdapter);
        }
    }


    /**
     * 开启时间计数器
     */
    public void startTimer(){
        if(playService == null){
            return;
        }
        long start = playService.getCurrentPosition();
        long total = playService.getCurrentAudio().getDuration();
        // 停止
        stopTimer();
        // 设置值
        start = (start / 1000);
        total = (total / 1000) - start + 1;
        if(start < 0 || total < 0 || start > total){
            return;
        }
        // 开始
        progressTimer = Observable.intervalRange(start,total,0,1,TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong){
                        if(viewDelegate != null){
                            viewDelegate.setProgress(aLong.intValue() * 1000);
                        }
                    }
                });
    }

    /**
     * 停止计数器
     */
    public void stopTimer(){
        if(progressTimer != null && !progressTimer.isDisposed()){
            progressTimer.dispose();
        }
    }

    // 手势操作监听
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) { }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if(distanceY > 60){
            viewDelegate.showBottomSheet();
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {}

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
