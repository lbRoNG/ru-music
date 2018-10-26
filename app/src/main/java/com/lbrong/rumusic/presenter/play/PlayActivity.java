package com.lbrong.rumusic.presenter.play;

import android.arch.lifecycle.Observer;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;

import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.db.table.Song;
import com.lbrong.rumusic.common.event.EventStringKey;
import com.lbrong.rumusic.common.utils.DateUtils;
import com.lbrong.rumusic.common.utils.ImageUtils;
import com.lbrong.rumusic.common.utils.MusicHelper;
import com.lbrong.rumusic.common.utils.SendEventUtils;
import com.lbrong.rumusic.presenter.base.ActivityPresenter;
import com.lbrong.rumusic.service.PlayService;
import com.lbrong.rumusic.view.play.PlayDelegate;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author lbRoNG
 * @since 2018/10/23
 * 播放详情
 */
public class PlayActivity
        extends ActivityPresenter<PlayDelegate>
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    // 播放服务
    private PlayService playService;
    // 连接引用
    private ServiceConnection serviceConnection;
    // 进去条计时器
    private Disposable progressTimer;

    @Override
    protected Class<PlayDelegate> getDelegateClass() {
        return PlayDelegate.class;
    }

    @Override
    protected boolean isFullScreen() {
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
    protected void bindEvenListener() {
        super.bindEvenListener();
        viewDelegate.setOnClickListener(this, R.id.iv_back,R.id.iv_start,R.id.iv_pre,R.id.iv_next);
    }

    @Override
    protected void init() {
        super.init();
        // 绑定服务
        bindPlayService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解绑服务
        if (serviceConnection != null && playService != null) {
            unbindService(serviceConnection);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                this.onBackPressed();
                break;
            case R.id.iv_start:
                if(playService != null){
                    if(playService.isPlaying()){
                        playService.pause();
                    } else {
                        playService.continuePlay();
                    }
                }
                break;
            case R.id.iv_pre:
                break;
            case R.id.iv_next:
                break;
        }
    }

    @Override
    protected void initLiveDataObserver() {
        super.initLiveDataObserver();
        // 播放状态改变监听
        SendEventUtils.observe(EventStringKey.Music.MUSIC_STATE, String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        if (!TextUtils.isEmpty(s)) {
                            switch (s) {
                                case EventStringKey.Music.MUSIC_PLAY:
                                    break;
                                case EventStringKey.Music.MUSIC_CONTINUE_PLAY:
                                    viewDelegate.setPlayIcon(true);
                                    if(playService != null){
                                        int start = playService.getCurrentPosition() / 1000;
                                        startProgressTimer(start);
                                    }
                                    break;
                                case EventStringKey.Music.MUSIC_PAUSE:
                                    viewDelegate.setPlayIcon(false);
                                    clearProgressTimer();
                                    break;
                                case EventStringKey.Music.MUSIC_STOP:
                                    viewDelegate.setPlayIcon(false);
                                    clearProgressTimer();
                                    break;
                                case EventStringKey.Music.MUSIC_RE_PLAY:
                                    viewDelegate.setPlayIcon(true);
                                    break;
                                case EventStringKey.Music.MUSIC_SEEK_TO:
                                    if(playService != null){
                                        int start = playService.getCurrentPosition() / 1000;
                                        startProgressTimer(start);
                                    }
                                    break;
                                case EventStringKey.Music.MUSIC_COMPLETE:
                                    viewDelegate.setPlayIcon(false);
                                    clearProgressTimer();
                                    break;
                            }
                        }
                    }
                });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            startProgressTimer(progress);
            if (playService != null) {
                playService.seekTo(progress * 1000);
                playService.continuePlay();
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) { }

    /**
     * 绑定服务
     */
    private void bindPlayService() {
        if (playService == null) {
            Intent intent = new Intent(this, PlayService.class);
            bindService(intent, serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    // 拿到服务引用
                    PlayService.PlayBinder binder = (PlayService.PlayBinder) service;
                    playService = binder.getService();
                    initSongInfo();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    playService = null;
                }
            }, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * 获取正在播放的歌曲信息，并设置UI
     */
    private void initSongInfo() {
        if (playService != null) {
            final Song item = playService.getCurrentAudio();
            if (item != null) {
                // 基础信息
                long currentMs = playService.getCurrentPosition();
                long totalMs = playService.getDuration();
                String current = DateUtils.getDateString(currentMs, "mm:ss");
                String total = DateUtils.getDateString(totalMs, "mm:ss");
                viewDelegate.setSongListName(item.getController().getIntoSongList())
                        .setSongName(item.getTitle())
                        .setSinger(item.getArtist())
                        .setCurrentDuration(current)
                        .setTotalDuration(total)
                        .setSeekBarListener(this)
                        .setCurrentSeekBar((int) currentMs / 1000)
                        .setCurrentProgressBar((int) currentMs / 1000)
                        .setMaxProgressBar((int) totalMs / 1000)
                        .setMaxSeekBar((int) totalMs / 1000);
                // 进度条同步
                if (playService.isPlaying()) {
                    startProgressTimer(playService.getCurrentPosition() / 1000);
                }
                // 按钮同步
                viewDelegate.setPlayIcon(playService.isPlaying());
                // 获取封面
                addDisposable(
                        Flowable.fromCallable(new Callable<Bitmap>() {
                            @Override
                            public Bitmap call() {
                                Bitmap cover = MusicHelper.build().getAlbumArt(item.getUrl(), 0);
                                if (cover == null) {
                                    Drawable drawable = ContextCompat.getDrawable(
                                            PlayActivity.this, R.drawable.ic_play_detail_cover_default);
                                    cover = ImageUtils.drawableToBitmap(drawable);
                                }
                                return cover;
                            }
                        }).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<Bitmap>() {
                                    @Override
                                    public void accept(Bitmap bitmap) {
                                        viewDelegate.setSongCover(bitmap);
                                    }
                                })
                );
            }
        }
    }

    /**
     * 停止计时器
     */
    private void clearProgressTimer(){
        if (progressTimer != null && !progressTimer.isDisposed()) {
            progressTimer.dispose();
        }
    }

    /**
     * 开启进度条计时器
     */
    private void startProgressTimer(long progress) {
        // 停止旧任务
        clearProgressTimer();

        // 重新开启
        long max = playService.getDuration() / 1000;
        addDisposable(progressTimer = Observable.intervalRange(progress + 1, max
                , 0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long duration) {
                        int progress = Integer.parseInt(duration + "");
                        viewDelegate
                                .setCurrentProgressBar(progress)
                                .setCurrentSeekBar(progress);
                        viewDelegate.setCurrentDuration(DateUtils.getDateString(playService.getCurrentPosition(), "mm:ss"));
                    }
                }));
    }
}
