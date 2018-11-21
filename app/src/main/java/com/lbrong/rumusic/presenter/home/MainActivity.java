package com.lbrong.rumusic.presenter.home;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.adapter.BasicSongListAdapter;
import com.lbrong.rumusic.common.db.table.PlayList;
import com.lbrong.rumusic.common.db.table.Song;
import com.lbrong.rumusic.common.event.music.MusicState;
import com.lbrong.rumusic.common.net.rx.subscriber.DefaultSubscriber;
import com.lbrong.rumusic.common.utils.EncryptionUtils;
import com.lbrong.rumusic.common.utils.MusicHelper;
import com.lbrong.rumusic.common.utils.ObjectHelper;
import com.lbrong.rumusic.common.utils.PermissionPageUtils;
import com.lbrong.rumusic.common.utils.SendEventUtils;
import com.lbrong.rumusic.iface.listener.OnPlayControllerClickListener;
import com.lbrong.rumusic.presenter.base.ActivityPresenter;
import com.lbrong.rumusic.service.PlayService;
import com.lbrong.rumusic.view.home.MainDelegate;
import com.lbrong.rumusic.view.widget.ErrorView;
import com.tbruyelle.rxpermissions2.RxPermissions;
import org.greenrobot.eventbus.Subscribe;
import org.litepal.LitePal;
import org.litepal.crud.callback.FindCallback;
import org.litepal.crud.callback.UpdateOrDeleteCallback;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @author lbRoNG
 * @since 2018/11/19
 */
public class MainActivity
       extends ActivityPresenter<MainDelegate>
       implements NavigationView.OnNavigationItemSelectedListener
        ,BaseQuickAdapter.OnItemClickListener,OnPlayControllerClickListener {

    // 适配器
    private BasicSongListAdapter songListAdapter;
    // 播放服务
    private PlayService playService;
    // 连接引用
    private ServiceConnection serviceConnection;
    // 最后点击的时间戳
    private long lastClickMS;

    @Override
    protected Class<MainDelegate> getDelegateClass() {
        return MainDelegate.class;
    }

    @Override
    protected void bindEvenListener() {
        super.bindEvenListener();
        viewDelegate.setNavigationItemSelectedListener(this);
        viewDelegate.setOnListScrollListener(new OnListScrollListener());
        viewDelegate.setControllerCallback(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_search:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void init() {
        super.init();
        // 事件接收
        SendEventUtils.register(this);
        // 获取权限
        judgePermission();
        // 绑定服务
        bindPlayService();
    }

    @Override
    protected void onStart() {
        if(playService != null && playService.isPlaying()){
            viewDelegate.resumeController(playService.getCurrentPosition());
            if(songListAdapter != null){
                songListAdapter.startTimer(playService.getCurrentPosition());
            }
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if(playService != null && playService.isPlaying()){
            viewDelegate.pauseController();
            if(songListAdapter != null){
                songListAdapter.stopTimer();
            }
        }
        super.onStop();
    }

    /**
     * 歌曲item点击
     */
    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        long currentMS = System.currentTimeMillis();
        // 防止点击过快
        if(currentMS - lastClickMS >= 500){
            Object temp = adapter.getItem(position);
            if (temp instanceof Song && playService != null) {
                Song click = (Song) temp;
                Song playing = playService.getCurrentAudio();
                if (!click.equals(playing)) {
                    // 后台开启播放
                    startPlay(click);
                } else {
                    // 重新播放
                    replay();
                }
            }
        }
        lastClickMS = currentMS;
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(MusicState state){
        switch (state){
            // 新歌曲播放
            case MUSIC_PLAY:
                // 更新列表
                if(songListAdapter != null && playService != null){
                    songListAdapter.setPlaying(playService.getCurrentAudio(),true);
                }
                // 设置并显示控制器
                showController();
                viewDelegate.setPlayBtn(true);
                break;
            case MUSIC_COMPLETE:
                // 播放完成停止控制器动画
                viewDelegate.pauseController();
                viewDelegate.setPlayBtn(false);
                break;
            case MUSIC_ALL_COMPLETE:
                // 播放完成停止控制器动画
                viewDelegate.pauseController();
                viewDelegate.setPlayBtn(false);
                break;
        }
    }

    /**
     * 控制器播放状态
     */
    @Override
    public void onAudioPlay() {
        if(playService != null){
            playService.continuePlay();
            viewDelegate.resumeController(playService.getCurrentPosition());
            if(songListAdapter != null){
                songListAdapter.startTimer(playService.getCurrentPosition());
            }
        }
    }

    /**
     * 控制器暂停状态
     */
    @Override
    public void onAudioPause() {
        if(playService != null){
            playService.pause();
            viewDelegate.pauseController();
            if(songListAdapter != null){
                songListAdapter.stopTimer();
            }
        }
    }

    /**
     * 控制器下一曲
     */
    @Override
    public void onAudioNext() {
        viewDelegate.hideController();
    }

    /**
     * 判断外部存储的权限
     */
    private void judgePermission() {
        RxPermissions permissions = new RxPermissions(this);
        // 判断是否已有权限
        boolean isGranted = permissions.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (isGranted) {
            // 已通过授权
            getLocalMusic();
        } else {
            boolean isRevoked = permissions.isRevoked(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (!isRevoked) {
                // 未申请
                addDisposable(permissions
                        .request(Manifest.permission.READ_EXTERNAL_STORAGE
                                , Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) {
                                if (aBoolean) {
                                    getLocalMusic();
                                } else {
                                    // 刷新ErrorView提示
                                    ErrorView errorView = (ErrorView) viewDelegate.getErrorView();
                                    errorView.setText("允许读取外部存储权限才能获取本地音乐哦!").show();
                                    // 友好提示去设置权限
                                    showPermissionTip();
                                }
                            }
                        }));
            } else {
                showPermissionTip();
            }
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
                    // 同步播放列表
                    syncPlayList();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    playService = null;
                }
            }, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * 友好提示获取权限
     */
    private void showPermissionTip() {
        new MaterialDialog.Builder(this)
                .content("允许读取外部存储权限才能获取本地音乐哦!")
                .positiveColorRes(R.color.colorAccent)
                .positiveText("去设置")
                .negativeText("不要")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        new PermissionPageUtils(MainActivity.this).jumpPermissionPage();
                    }
                })
                .build()
                .show();
    }

    /**
     * 获取本地音乐
     * 主要通过自身数据库管理，只在数据库没有歌曲的时候，去扫描媒体库，其余时候都直接使用本地数据库
     */
    private void getLocalMusic() {
        addDisposable(
                Flowable.fromCallable(new Callable<List<Song>>() {
                    @Override
                    public List<Song> call() {
                        return LitePal.findAll(Song.class);
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .map(new Function<List<Song>, List<Song>>(){
                            @Override
                            public List<Song> apply(List<Song> songs){
                                if(!ObjectHelper.requireNonNull(songs)){
                                    // 本地扫描
                                    songs = MusicHelper.build().getLocalMusic(MainActivity.this);
                                    // 添加到数据库
                                    LitePal.saveAll(songs);
                                }
                                return songs;
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DefaultSubscriber<List<Song>>(){
                            @Override
                            public void onNext(List<Song> songs) {
                                if (ObjectHelper.requireNonNull(songs)) {
                                    // 隐藏
                                    viewDelegate.getErrorView().hide();
                                    // 创建适配器
                                    songListAdapter = new BasicSongListAdapter(songs){
                                        @Override
                                        protected void asyncCover(ImageView view, Song item) {
                                            // todo 重新加载封面
                                        }
                                    };
                                    // 设置
                                    songListAdapter.setOnItemClickListener(MainActivity.this);
                                    songListAdapter.bindToRecyclerView((RecyclerView) viewDelegate.get(R.id.rv_list));
                                    viewDelegate.setSongListAdapter(songListAdapter);
                                } else {
                                    ErrorView errorView = (ErrorView) viewDelegate.getErrorView();
                                    errorView.setText("没有本地音乐哦，快去搜索添加吧！").show();
                                }
                            }
                        })
        );
    }

    /**
     * 开始播放
     */
    private void startPlay(final Song item) {
        // 当前歌曲集合
        final List<Song> songs = songListAdapter.getData();
        // 播放列表更新
        // 比对保存的播放列表和当前要设置的播放列表是否相同
        PlayList playList = playService.getPlayList();
        String serviceMD5 = playList == null ? "" : EncryptionUtils.md5(playList.getSongs().toString());
        String nowMD5 = EncryptionUtils.md5(songs.toString());
        if(!TextUtils.equals(serviceMD5,nowMD5)){
            LitePal.deleteAllAsync(PlayList.class)
                    .listen(new UpdateOrDeleteCallback() {
                        @Override
                        public void onFinish(int rowsAffected) {
                            // 创建播放列表
                            PlayList playList = new PlayList();
                            playList.setCount(songs.size());
                            playList.setSongs(songs);
                            // 设置给服务并播放
                            playService.setPlayList(playList);
                            playService.setAudio(item);
                            playService.playAudio();
                            // 保存
                            playList.saveAsync();
                        }
                    });
        } else {
            // 通知服务播放
            playService.setAudio(item);
            playService.playAudio();
        }
    }

    /**
     * 重新播放
     */
    private void replay() {
        playService.rePlay();
    }

    /**
     * 同步播放列表
     */
    private void syncPlayList(){
        LitePal.findFirstAsync(PlayList.class)
                .listen(new FindCallback<PlayList>() {
            @Override
            public void onFinish(PlayList playList) {
                // 不为空证明设置过播放列表
                if(ObjectHelper.requireNonNull(playList)){
                    // 播放列表给到服务保存
                    playService.setPlayList(playList);
                    // 恢复播放记录
                    recoverPlayRecord(playList);
                }
            }
        });
    }

    /**
     * 设置控制器
     */
    private void showController(){
        if(playService != null){
            Song plying = playService.getCurrentAudio();
            if(plying != null){
                viewDelegate.setController(plying.getCover(),plying.getTitle()
                        ,plying.getArtist(),plying.getDuration(),playService.getCurrentPosition(),true);
                viewDelegate.showController();
            }
        }
    }

    /**
     * 恢复播放记录
     * @param playList 当前播放列表
     */
    private void recoverPlayRecord(final PlayList playList){
        long currentId = playList.getPlayingId();
        final Song plying = LitePal.where("songid=" + String.valueOf(currentId)).findFirst(Song.class);
        if(plying != null){
            // 恢复服务
            playService.setPlayList(playList);
            playService.rePrepare(plying,(int)playList.getRecord());
            // 确保适配器初始化完成
            addDisposable(
                    Observable.fromCallable(new Callable<Boolean>() {
                        @Override
                        public Boolean call() {
                            while (true){
                                if(songListAdapter != null){
                                    break;
                                }
                            }
                            return true;
                        }
                    }).subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .flatMap(new Function<Boolean, ObservableSource<Long>>() {
                                @Override
                                public ObservableSource<Long> apply(Boolean aBoolean)  {
                                    songListAdapter.setPlaying(plying,false);
                                    return Observable.timer(500,TimeUnit.MICROSECONDS,Schedulers.computation());
                                }
                    }).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<Long>() {
                                @Override
                                public void accept(Long aLong) {
                                    songListAdapter.setItemDuration(playList.getRecord());
                                }
                            })
            );
            // 恢复控制器
            viewDelegate.setController(plying.getCover(),plying.getTitle()
                    ,plying.getArtist(),plying.getDuration(),playService.getCurrentPosition(),false);
            viewDelegate.showController();
            viewDelegate.setPlayBtn(false);
            viewDelegate.setProgress(playList.getRecord());
        }
    }

    /**
     * 滑动监听
     * 保证正在播放的歌曲item样式，不可见时不要计时，可见时再计时
     */
    private class OnListScrollListener extends RecyclerView.OnScrollListener{
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if(songListAdapter != null && playService != null){
                int start = viewDelegate.getLayoutManager().findFirstVisibleItemPosition();
                int end = viewDelegate.getLayoutManager().findLastVisibleItemPosition();
                int playing = songListAdapter.getPlayingPos();
                if(playing >= start && playing <= end){
                    if(songListAdapter.isTimerStop()){
                        int now = playService.getCurrentPosition();
                        if(!playService.isPlaying()){
                            songListAdapter.setItemDuration(now);
                        } else {
                            songListAdapter.startTimer(now);
                        }
                    }
                } else {
                    if(!songListAdapter.isTimerStop()){
                        songListAdapter.stopTimer();
                    }
                }
            }
        }
    }
}
