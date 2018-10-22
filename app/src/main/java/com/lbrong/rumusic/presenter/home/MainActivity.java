package com.lbrong.rumusic.presenter.home;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.lbrong.rumusic.R;
import com.lbrong.rumusic.iface.callback.OnBindPlayServiceSuccess;
import com.lbrong.rumusic.presenter.base.ActivityPresenter;
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
       implements NavigationView.OnNavigationItemSelectedListener {

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
            viewDelegate.showController();
            int max = (int) (playService.getCurrentAudio().getDuration() / 1000);
            viewDelegate.initController(playService.getCurrentAudio().getArtist()
                    ,playService.getCurrentAudio().getTitle(),1,max);
            addDisposable(
                    controllerTimer = Observable.intervalRange(1,max,0,1,TimeUnit.SECONDS)
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
}
