package com.lbrong.rumusic.presenter.mine;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.bean.Song;
import com.lbrong.rumusic.common.adapter.SongListAdapter;
import com.lbrong.rumusic.common.event.EventStringKey;
import com.lbrong.rumusic.common.event.home.PageRefresh;
import com.lbrong.rumusic.common.net.rx.subscriber.DefaultSubscriber;
import com.lbrong.rumusic.common.utils.MusicHelper;
import com.lbrong.rumusic.common.utils.ObjectHelper;
import com.lbrong.rumusic.common.utils.PermissionPageUtils;
import com.lbrong.rumusic.common.utils.SendEventUtils;
import com.lbrong.rumusic.iface.callback.OnBindPlayServiceSuccess;
import com.lbrong.rumusic.presenter.base.FragmentPresenter;
import com.lbrong.rumusic.presenter.home.MainActivity;
import com.lbrong.rumusic.service.PlayService;
import com.lbrong.rumusic.view.mine.MineDelegate;
import com.lbrong.rumusic.view.widget.ErrorView;
import com.tbruyelle.rxpermissions2.RxPermissions;
import java.util.List;
import java.util.concurrent.Callable;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author lbRoNG
 * @since 2018/10/18
 */
public class MineFragment
        extends FragmentPresenter<MineDelegate>
        implements BaseQuickAdapter.OnItemClickListener, BaseQuickAdapter.OnItemChildClickListener{

    // 播放服务
    private PlayService playService;
    // 适配器
    private SongListAdapter songAdapter;

    @Override
    protected Class<MineDelegate> getDelegateClass() {
        return MineDelegate.class;
    }

    @Override
    protected void init() {
        super.init();
        // 获取权限
        judgePermission();
    }

    @Override
    protected void bindEvenListener() {
        super.bindEvenListener();
        viewDelegate.setOnListScrollListener(new OnListScrollListener());
    }

    @Override
    protected void initLiveDataObserver() {
        super.initLiveDataObserver();
        // 下拉刷新监听
        SendEventUtils.observe(EventStringKey.Home.PAGE_REFRESH,PageRefresh.class)
                .observe(this, new Observer<PageRefresh>() {
                    @Override
                    public void onChanged(@Nullable PageRefresh pageRefresh) {
                        if(pageRefresh != null && pageRefresh.getIndex() == 0){
                            //getLocalMusic();
                            if(getMain() != null){
                                getMain().hideRefresh();
                            }
                        }
                    }
                });
        // 播放状态改变监听
        SendEventUtils.observe(EventStringKey.Music.MUSIC_STATE,String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        if(!TextUtils.isEmpty(s)){
                            switch (s){
                                case EventStringKey.Music.MUSIC_PLAY:
                                    break;
                                case EventStringKey.Music.MUSIC_CONTINUE_PLAY:
                                    if(songAdapter != null && playService != null){
                                        int start = playService.getCurrentPosition() / 1000;
                                        songAdapter.startProgressTimer(start);
                                    }
                                    break;
                                case EventStringKey.Music.MUSIC_PAUSE:
                                    if(songAdapter != null){
                                        songAdapter.clearProgressTimer();
                                    }
                                    break;
                                case EventStringKey.Music.MUSIC_STOP:
                                    if(songAdapter != null){
                                        songAdapter.clearProgressTimer();
                                        songAdapter.resetSongState();
                                    }
                                    break;
                                case EventStringKey.Music.MUSIC_RE_PLAY:
                                    break;
                                case EventStringKey.Music.MUSIC_SEEK_TO:
                                    if(songAdapter != null && playService != null){
                                        int start = playService.getCurrentPosition() / 1000;
                                        songAdapter.startProgressTimer(start);
                                    }
                                    break;
                                case EventStringKey.Music.MUSIC_COMPLETE:
                                    if(songAdapter != null){
                                        int playing = songAdapter.getPlayingSongPos();
                                        int next = playing + 1;
                                        if(next >= songAdapter.getData().size()){
                                            next = 0;
                                        }
                                        View view = songAdapter.getViewByPosition(next,R.id.rl_root);
                                        songAdapter.setOnItemClick(view,next);
                                    }
                                    break;
                            }
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        if (songAdapter != null) {
            songAdapter.clearProgressTimer();
        }
        super.onDestroy();
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Object temp = adapter.getItem(position);
        if (temp instanceof Song) {
            if (songAdapter != null) {
                if (songAdapter.getPlayingSongPos() != position) {
                    // 复位上个播放的歌曲样式
                    songAdapter.resetSongState();
                    // 更新新播放歌曲的样式
                    Song item = (Song) temp;
                    item.getController().setPlaying(true);
                    songAdapter.updatePlayingSongPos(position);
                    // 播放
                    startPlay(item);
                } else {
                    // 重新播放
                    songAdapter.updatePlayingSongPos(position);
                    replay();
                }
            }
        }
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        viewDelegate.toast("child");
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
     * 友好提示获取权限
     */
    private void showPermissionTip() {
        new MaterialDialog.Builder(getActivity())
                .content("允许读取外部存储权限才能获取本地音乐哦!")
                .positiveColorRes(R.color.colorAccent)
                .positiveText("去设置")
                .negativeText("不要")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        new PermissionPageUtils(getActivity()).jumpPermissionPage();
                    }
                })
                .build()
                .show();
    }

    /**
     * 获取本地音乐
     */
    private void getLocalMusic() {
        addDisposable(
                Flowable.fromCallable(new Callable<List<Song>>() {
                    @Override
                    public List<Song> call() {
                        return MusicHelper.build().getLocalMusic(getActivity());
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DefaultSubscriber<List<Song>>(){
                            @Override
                            public void onFinish() {
                                super.onFinish();
                                if(getMain() != null){
                                    getMain().hideRefresh();
                                }
                            }

                            @Override
                            protected void onStart() {
                                super.onStart();
                                if(getMain() != null){
                                    getMain().showRefresh();
                                }
                            }

                            @Override
                            public void onNext(List<Song> songs) {
                                if (ObjectHelper.requireNonNull(songs)) {
                                    viewDelegate.getErrorView().hide();

                                    if (songAdapter != null) {
                                        songAdapter.setNewData(songs);
                                        return;
                                    }

                                    songAdapter = new SongListAdapter(songs) {
                                        @Override
                                        protected void asyncCover(final ImageView view, final Song item) {
                                            addDisposable(
                                                    Flowable.fromCallable(new Callable<Bitmap>() {
                                                        @Override
                                                        public Bitmap call() {
                                                            return MusicHelper.build().getAlbumArt(item.getUrl(), 8);
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

                                        @Override
                                        protected void seekBarChange(Song item, int progress) {
                                            super.seekBarChange(item, progress);
                                            if(playService != null){
                                                playService.seekTo(progress);
                                            }
                                        }
                                    };
                                    songAdapter.setOnItemClickListener(MineFragment.this);
                                    songAdapter.setOnItemChildClickListener(MineFragment.this);
                                    songAdapter.bindToRecyclerView((RecyclerView) viewDelegate.get(R.id.rv_song_list));
                                    viewDelegate.setSongListAdapter(songAdapter);
                                } else {
                                    ErrorView errorView = (ErrorView) viewDelegate.getErrorView();
                                    errorView.setText("没有本地音乐哦，快去搜索添加吧！").show();
                                }
                            }
                        })
        );
    }

    /**
     * 获取依附的Activity
     */
    private @Nullable
    MainActivity getMain(){
        if(getActivity() instanceof MainActivity){
            return (MainActivity) getActivity();
        }
        return null;
    }

    /**
     * 开始播放
     */
    private void startPlay(final Song item) {
        final MainActivity activity = getMain();
        if (songAdapter != null && activity != null) {
            // 复位进度，重新开始播放
            songAdapter.startProgressTimer(0);
            // 判断服务是否绑定,没绑定先绑定服务
            if (playService == null) {
                activity.bindPlayService(new OnBindPlayServiceSuccess() {
                    @Override
                    public void success(PlayService service) {
                        // 开始播放
                        playService = service;
                        service.setAudio(item);
                        service.playAudio();
                        activity.setPlayController();
                    }
                });
            } else {
                // 播放音乐
                playService.setAudio(item);
                playService.playAudio();
                activity.setPlayController();
            }
        }
    }

    /**
     * 重新播放
     */
    private void replay() {
        if (songAdapter != null && playService != null) {
            songAdapter.startProgressTimer(0);
            playService.rePlay();
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
            if(songAdapter != null && playService != null){
                int start = viewDelegate.getLayoutManager().findFirstVisibleItemPosition();
                int end = viewDelegate.getLayoutManager().findLastVisibleItemPosition();
                int playing = songAdapter.getPlayingSongPos();
                if(playing >= start && playing <= end){
                    int s = playService.getCurrentPosition() / 1000;
                    if(!playService.isPlaying()){
                        songAdapter.setItemProgress(s);
                    } else {
                        songAdapter.startProgressTimer(s);
                    }
                } else {
                    songAdapter.clearProgressTimer();
                }
            }
        }
    }

}
