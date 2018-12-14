package com.lbrong.rumusic.presenter.play;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.widget.PopupMenu;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.GestureDetector;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;

import com.chad.library.adapter.base.BaseQuickAdapter;
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

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class PlayActivity
       extends ActivityPresenter<PlayDelegate>
       implements GestureDetector.OnGestureListener,OnPlayMethodChangeListener,View.OnClickListener
        ,SeekBar.OnSeekBarChangeListener,BaseQuickAdapter.OnItemClickListener,BaseQuickAdapter.OnItemChildClickListener
        ,PopupMenu.OnMenuItemClickListener{

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
    // 播放列表最后的点击时间，防止点击过快
    private long lastClickMS;
    // 播放列表显示菜单的item在列表中的位置
    private int playListMenuIndex = -1;

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
    public void onBackPressed() {
        supportFinishAfterTransition();
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
        viewDelegate.setOnClickListener(this,R.id.iv_play,R.id.iv_previous,R.id.iv_next,R.id.iv_random);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(MusicState state){
        switch (state){
            // 新歌曲播放
            case MUSIC_PLAY:
                updatePlayingSong();
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
        // 是否显示重新随机的icon
        viewDelegate.get(R.id.iv_random).setVisibility(method == PlayMethodEnum.RANDOM ? View.VISIBLE : View.GONE);

        if(playListSongAdapter != null){
            // 随机切换到顺序和顺序切换到随机
            if((SettingHelper.build().getPlayMethod() == PlayMethodEnum.RANDOM && method != PlayMethodEnum.RANDOM )
                    || (SettingHelper.build().getPlayMethod() != PlayMethodEnum.RANDOM && method == PlayMethodEnum.RANDOM)){
                // 保存播放方式
                SettingHelper.build().playMethod(method);
                playListSongAdapter.task();
                return;
            }
        }

        // 保存播放方式
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
            case R.id.iv_random:
                if(playService != null){
                    PlayList playList = playService.getPlayList();
                    long seed = System.currentTimeMillis() + new Random().nextLong();
                    playList.setSeed(seed);
                    playListSongAdapter.task();
                    playList.updateAsync(playList.getId());
                }
                break;
        }
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        playListMenuIndex = position;
        showPlayListMenu(view);
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        long currentMS = System.currentTimeMillis();
        if(currentMS - lastClickMS >= 500){
            Object obj = adapter.getItem(position);
            if(obj instanceof Song && playService != null && playListSongAdapter != null){
                Song item = (Song) obj;
                playService.setAudio(item);
                playService.playAudio();
            }
        }
        lastClickMS = currentMS;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (playListMenuIndex != -1 && playService != null){
            // 通过播放方式拿到播放列表
            List<Song> playSongs = playListSongAdapter.getData();
            // 正在选中的歌曲
            Song select = playSongs.get(playListMenuIndex);
            // 分析动作
            switch (item.getItemId()){
                // 马上播放
                case R.id.action_play:
                    playService.setAudio(select);
                    playService.playAudio();
                    break;
                // 下一首播放
                case R.id.action_next:

                    break;
                // 重复播放
                case R.id.action_repeat:

                    break;
                // 取消歌曲重复
                case R.id.action_cancel_repeat:

                    break;
            }
            // 重置索引
            playListMenuIndex = -1;
        }
        return false;
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
        // 播放列表
        PlayList playList = playService.getPlayList();
        playListSongAdapter = new PlayPlayListSongAdapter(playList){
            @Override
            protected void taskComplete() {
                viewDelegate.setPlayListPlayingNo(playListSongAdapter.getPlayingPos() + 1,
                        playListSongAdapter.getItemCount());
            }
        };
        playListSongAdapter.setOnItemClickListener(this);
        playListSongAdapter.setOnItemChildClickListener(this);
        viewDelegate.setPlayListSongsAdapter(playListSongAdapter);
        // 是否显示重新随机的icon
        viewDelegate.get(R.id.iv_random).setVisibility(
                SettingHelper.build().getPlayMethod() == PlayMethodEnum.RANDOM ? View.VISIBLE : View.GONE);
    }

    /**
     * 更新播放列表
     */
    private void updatePlayingSong(){
        if(playService != null && playListSongAdapter != null){
            Song playing = playService.getCurrentAudio();
            // 歌曲信息
            viewDelegate.setMusicInfo(playing.getCover(),playing.getTitle(),playing.getArtist(),playService.isPlaying());
            // 进度条信息
            viewDelegate.setMusicDuration(playing.getDuration(),playService.getCurrentPosition());
            // 播放列表
            playListSongAdapter.setPlaying(playService.getCurrentAudio());
            viewDelegate.setPlayListPlayingNo(playListSongAdapter.getPlayingPos() + 1,
                    playListSongAdapter.getItemCount());
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
        if(start < 0 || total < 0){
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

    /**
     * 显示播放列表菜单
     * @param anchor 锚点
     */
    public void showPlayListMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.setOnMenuItemClickListener(this);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.play_list_controll, popup.getMenu());
        popup.show();
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
        if(distanceY > 60 && playListSongAdapter != null){
            viewDelegate.showBottomSheet();
            viewDelegate.scrollToPlayingAsPlayList(playListSongAdapter.getPlayingPos());
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
