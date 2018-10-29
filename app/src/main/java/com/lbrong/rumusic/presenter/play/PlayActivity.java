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
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.adapter.BasicPlayListAdapter;
import com.lbrong.rumusic.common.db.table.Song;
import com.lbrong.rumusic.common.event.EventStringKey;
import com.lbrong.rumusic.common.type.PlayMethodEnum;
import com.lbrong.rumusic.common.utils.DateUtils;
import com.lbrong.rumusic.common.utils.ImageUtils;
import com.lbrong.rumusic.common.utils.MusicHelper;
import com.lbrong.rumusic.common.utils.SendEventUtils;
import com.lbrong.rumusic.common.utils.SettingHelper;
import com.lbrong.rumusic.iface.listener.OnPlayMethodChangeListener;
import com.lbrong.rumusic.presenter.base.ActivityPresenter;
import com.lbrong.rumusic.service.PlayService;
import com.lbrong.rumusic.view.play.PlayDelegate;
import org.litepal.LitePal;
import java.lang.ref.WeakReference;
import java.util.List;
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
       implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, OnPlayMethodChangeListener {

    // 播放服务
    private PlayService playService;
    // 连接引用
    private ServiceConnection serviceConnection;
    // 进去条计时器
    private Disposable progressTimer;
    // 是否列表播放完成
    private boolean isMusicAllComplete;
    // 播放列表适配器
    private BasicPlayListAdapter playListAdapter;

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
        viewDelegate.setOnClickListener(this, R.id.iv_back,R.id.iv_start,R.id.iv_pre,R.id.iv_next,
                R.id.iv_sort_more_2);
        viewDelegate.setOnPlayMethodChangeListener(this);
    }

    @Override
    protected void init() {
        super.init();
        // 绑定服务
        bindPlayService();
        // 设置播放方式默认值
        setPlayMethod();
    }

    @Override
    protected void onDestroy() {
        // 解除动画
        stopCDRotateAnim();
        // 解绑服务
        if (serviceConnection != null && playService != null) {
            unbindService(serviceConnection);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(viewDelegate.isDrawerShow()){
            viewDelegate.closeDrawer();
        } else {
            super.onBackPressed();
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
            case R.id.iv_pre:
                if(playService != null){
                    playService.previous();
                }
                break;
            case R.id.iv_next:
                if(playService != null){
                    playService.next(true);
                }
                break;
            case R.id.iv_sort_more_2:
                viewDelegate.openDrawer();
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
                                    // 歌曲播放
                                    initSongInfo();
                                    // cd动画
                                    startCDRotateAnim();
                                    // 播放列表
                                    if(playListAdapter != null && playService != null){
                                        playListAdapter.updatePlayingSongPos(playService.getCurrentAudio());
                                    }
                                case EventStringKey.Music.MUSIC_RE_PLAY:
                                    // 重新播放
                                    viewDelegate.setPlayIcon(true);
                                    startProgressTimer(0);
                                    break;
                                case EventStringKey.Music.MUSIC_CONTINUE_PLAY:
                                    // 继续播放
                                    // cd动画
                                    startCDRotateAnim();
                                case EventStringKey.Music.MUSIC_SEEK_TO:
                                    // 继续播放
                                    viewDelegate.setPlayIcon(true);
                                    if(playService != null){
                                        int start = playService.getCurrentPosition() / 1000;
                                        startProgressTimer(start);
                                    }
                                    break;
                                case EventStringKey.Music.MUSIC_PAUSE:
                                    // 暂停
                                case EventStringKey.Music.MUSIC_STOP:
                                    // 停止
                                case EventStringKey.Music.MUSIC_COMPLETE:
                                    // 单首播放完成
                                    viewDelegate.setPlayIcon(false);
                                    clearProgressTimer();
                                    // cd动画
                                    stopCDRotateAnim();
                                    break;
                                case EventStringKey.Music.MUSIC_ALL_COMPLETE:
                                    // 播放列表播放完毕，并且不循环
                                    isMusicAllComplete = true;
                                    // cd动画
                                    stopCDRotateAnim();
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

    @Override
    public void onPlayMethodChange(PlayMethodEnum method) {
        SettingHelper.build().playMethod(method);
    }

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
                viewDelegate.setSongListName(playService.getSongListName())
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
                    startCDRotateAnim();
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
                // 设置播放列表
                if(playListAdapter == null){
                    setPlayList();
                }
            }
        }
    }

    /**
     * 设置播放方式选中的UI
     */
    private void setPlayMethod(){
        PlayMethodEnum method = SettingHelper.build().getPlayMethod();
        viewDelegate.setPlayMethod(method);
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

    /**
     * 开始cd旋转动画
     */
    private void startCDRotateAnim(){
        if(playService != null){
            // 计算旋转圈数
            // 总时间，最大设置60分钟一首
            long total = 60 * 60 * 1000;
            // 一圈毫秒值
            long one = 5000;
            // 圈数
            long count = total / one;
            // 设置动画
            View cover = viewDelegate.get(R.id.iv_cover_bg);
            // 取消原有动画
            stopCDRotateAnim();
            // 开启动画
            cover.animate()
                    .setStartDelay(0)
                    .setInterpolator(new LinearInterpolator())
                    .rotation(count * 360f)
                    .setDuration(total);
        }
    }

    /**
     * 停止cd旋转动画
     */
    private void stopCDRotateAnim(){
        View cover = viewDelegate.get(R.id.iv_cover_bg);
        cover.animate().cancel();
    }

    /**
     * 设置播放列表
     */
    private void setPlayList(){
        addDisposable(
                Observable.fromCallable(new Callable<List<Song>>(){
                    @Override
                    public List<Song> call(){
                        // 获取播放列表ids
                        List<Long> ids = playService.getSongListIds();
                        // 获取对应的歌曲集合
                        long[] temp = new long[ids.size()];
                        for (int i = 0; i < ids.size(); i++) {
                            temp[i] = ids.get(i);
                        }
                        // 数据库取出对应数据
                        List<Song> songs = LitePal.findAll(Song.class,temp);
                        // 更新更在播放的音乐状态
                        Song playing = playService.getCurrentAudio();
                        int index = songs.indexOf(playing);
                        songs.remove(playing);
                        songs.add(index,playing);
                        return songs;
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Song>>() {
                    @Override
                    public void accept(List<Song> songs){
                        viewDelegate.setPlayListAdapter(
                                playListAdapter = new BasicPlayListAdapter(songs){
                                    @Override
                                    protected void asyncCover(final ImageView view,final Song item) {
                                        addDisposable(
                                                Flowable.fromCallable(new Callable<Bitmap>() {
                                                    @Override
                                                    public Bitmap call() {
                                                        Bitmap cover = MusicHelper.build().getAlbumArt(item.getUrl(), 0);
                                                        if(cover == null){
                                                            Drawable drawable = ContextCompat.getDrawable(
                                                                    PlayActivity.this,R.drawable.ic_mine_song_default_cover);
                                                            cover = ImageUtils.drawableToBitmap(drawable);
                                                        } else {
                                                            item.setBitmap(new WeakReference<>(cover));
                                                        }
                                                        return cover;
                                                    }
                                                }).subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(new Consumer<Bitmap>() {
                                                            @Override
                                                            public void accept(Bitmap bitmap) {
                                                                view.setImageBitmap(bitmap);
                                                            }
                                                        })
                                        );
                                    }
                                });
                    }
                })
        );
    }
}
