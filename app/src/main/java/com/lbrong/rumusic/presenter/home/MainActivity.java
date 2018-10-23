package com.lbrong.rumusic.presenter.home;

import android.app.ActivityOptions;
import android.arch.lifecycle.Observer;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.MenuItem;

import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.event.EventStringKey;
import com.lbrong.rumusic.common.event.home.PageRefresh;
import com.lbrong.rumusic.common.utils.SendEventUtils;
import com.lbrong.rumusic.iface.callback.OnBindPlayServiceSuccess;
import com.lbrong.rumusic.iface.listener.OnPlayControllerClickListener;
import com.lbrong.rumusic.presenter.base.ActivityPresenter;
import com.lbrong.rumusic.presenter.play.PlayActivity;
import com.lbrong.rumusic.service.PlayService;
import com.lbrong.rumusic.view.home.MainDelegate;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity
       extends ActivityPresenter<MainDelegate>
       implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener
        , OnPlayControllerClickListener {

    // 播放服务
    private PlayService playService;
    // 连接引用
    private ServiceConnection serviceConnection;
    // 控制器进度
    private Disposable controllerTimer;

    @Override
    protected Class<MainDelegate> getDelegateClass() {
        return MainDelegate.class;
    }

    @Override
    protected void bindEvenListener() {
        super.bindEvenListener();
        viewDelegate.setNavigationItemSelectedListener(this);
        viewDelegate.setOnRefreshListener(this);
        viewDelegate.setControllerClickListener(this);
    }

    @Override
    protected void init() {
        super.init();
        // 启动服务
        startService(new Intent(this,PlayService.class));
    }

    @Override
    protected void onDestroy() {
        if(serviceConnection != null && playService != null){
            unbindService(serviceConnection);
        }

        if(controllerTimer != null && !controllerTimer.isDisposed()){
            controllerTimer.dispose();
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = viewDelegate.get(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_search:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_play:
                break;
            case R.id.nav_cache:
                break;
            case R.id.nav_download:
                break;
            case R.id.nav_setting:
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_about:
                break;
        }

        DrawerLayout drawer = viewDelegate.get(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void initLiveDataObserver() {
        super.initLiveDataObserver();
        SendEventUtils.observe(EventStringKey.Music.MUSIC_STATE,String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        if(!TextUtils.isEmpty(s)){
                            int start = playService.getCurrentPosition() / 1000;
                            int max = (int) (playService.getCurrentAudio().getDuration() / 1000);
                            switch (s){
                                case EventStringKey.Music.MUSIC_PLAY:
                                    viewDelegate.showController();
                                    viewDelegate.setControllerStyle(1);
                                    break;
                                case EventStringKey.Music.MUSIC_CONTINUE_PLAY:
                                    if(playService != null){
                                        startPlayControllerTimer(start,max);
                                    }
                                    break;
                                case EventStringKey.Music.MUSIC_PAUSE:
                                    if(controllerTimer != null && !controllerTimer.isDisposed()){
                                        controllerTimer.dispose();
                                    }
                                    break;
                                case EventStringKey.Music.MUSIC_STOP:
                                    viewDelegate.hideController();
                                    viewDelegate.setControllerStyle(0);
                                    break;
                                case EventStringKey.Music.MUSIC_RE_PLAY:
                                    startPlayControllerTimer(0,max);
                                    viewDelegate.setControllerStyle(1);
                                    break;
                                case EventStringKey.Music.MUSIC_SEEK_TO:
                                    if(playService != null){
                                        startPlayControllerTimer(start,max);
                                        viewDelegate.setControllerStyle(1);
                                    }
                                    break;
                            }
                        }
                    }
                });
    }

    @Override
    public void onRefresh() {
        int index = viewDelegate.getSelectIndex();
        SendEventUtils.sendForMain(EventStringKey.Home.PAGE_REFRESH,new PageRefresh(index));
    }

    /**
     * 音乐播放
     */
    @Override
    public void onAudioPlay() {
        if(playService != null){
            playService.continuePlay();
        }
    }

    /**
     * 音乐暂停
     */
    @Override
    public void onAudioPause() {
        if(playService != null){
            playService.pause();
        }
    }

    @Override
    public void onGo() {
        Intent send = new Intent(this,PlayActivity.class);
        startActivity(send,ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    @Override
    public void onAudioNext() {
        SendEventUtils.sendForMain(EventStringKey.Music.MUSIC_STATE,EventStringKey.Music.MUSIC_COMPLETE);
    }

    /**
     * 显示刷新
     */
    public void showRefresh(){
        viewDelegate.showRefresh();
    }

    /**
     * 隐藏刷新
     */
    public void hideRefresh(){
        viewDelegate.hideRefresh();
    }

    /**
     * 绑定服务
     */
    public void bindPlayService(@Nullable final OnBindPlayServiceSuccess callback){
        if (playService == null) {
            Intent intent = new Intent(this, PlayService.class);
            bindService(intent, serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    // 拿到服务引用
                    PlayService.PlayBinder binder = (PlayService.PlayBinder) service;
                    playService = binder.getService();

                    if(callback != null ){
                        callback.success(playService);
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    playService = null;
                }
            }, Context.BIND_AUTO_CREATE);
        } else {
            if(callback != null ){
                callback.success(playService);
            }
        }
    }

    /**
     * 设置控制器
     */
    public void setPlayController(){
        if(playService != null){
            int max = (int) (playService.getCurrentAudio().getDuration() / 1000);
            viewDelegate.initController(playService.getCurrentAudio().getArtist()
                    ,playService.getCurrentAudio().getTitle(),1,max);
            startPlayControllerTimer(1,max);
        }
    }

    /**
     * 设置进度计数器
     */
    public void startPlayControllerTimer(int start,int max){
        if(controllerTimer != null && !controllerTimer.isDisposed()){
            controllerTimer.dispose();
        }
        addDisposable(
                controllerTimer = Observable.intervalRange(start+1,max,0,1,TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong){
                                viewDelegate.setControllerProgress(Integer.parseInt(aLong + ""));
                            }
                        })
        );
    }
}
