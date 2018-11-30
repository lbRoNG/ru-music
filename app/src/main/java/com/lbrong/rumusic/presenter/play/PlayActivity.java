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
import android.view.Window;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.db.table.Song;
import com.lbrong.rumusic.common.event.music.MusicState;
import com.lbrong.rumusic.common.utils.SendEventUtils;
import com.lbrong.rumusic.presenter.base.ActivityPresenter;
import com.lbrong.rumusic.service.PlayService;
import com.lbrong.rumusic.view.play.PlayDelegate;
import org.greenrobot.eventbus.Subscribe;

public class PlayActivity
       extends ActivityPresenter<PlayDelegate>
       implements GestureDetector.OnGestureListener {

    // 播放服务
    private PlayService playService;
    // 连接引用
    private ServiceConnection serviceConnection;
    // 手势监听
    private GestureDetector gestureDetector;

    @Override
    protected Class<PlayDelegate> getDelegateClass() {
        return PlayDelegate.class;
    }

    @Override
    protected boolean displayHomeAsUp() {
        return false;
    }

    @Override
    protected boolean isFullScreen() {
        return true;
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

    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(MusicState state){

    }

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
        viewDelegate.setMusicInfo(playing.getCover(),playing.getTitle(),playing.getArtist(),playService.isPlaying());
        viewDelegate.setMusicDuration(playing.getDuration(),playService.getCurrentPosition());
    }
}
