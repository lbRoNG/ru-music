package com.lbrong.rumusic.presenter.mine;

import android.Manifest;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.bean.Song;
import com.lbrong.rumusic.common.adapter.SongListAdapter;
import com.lbrong.rumusic.common.utils.MusicHelper;
import com.lbrong.rumusic.common.utils.ObjectHelper;
import com.lbrong.rumusic.common.utils.PermissionPageUtils;
import com.lbrong.rumusic.presenter.base.FragmentPresenter;
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
       implements BaseQuickAdapter.OnItemClickListener,BaseQuickAdapter.OnItemChildClickListener {

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
    public void onDestroy() {
        if(songAdapter != null){
            songAdapter.clearProgressTimer();
        }
        super.onDestroy();
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Object temp = adapter.getItem(position);
        if(temp instanceof Song){
            if(songAdapter != null){
                if(songAdapter.getPlayingSongPos() != position){
                    // 复位上个播放的歌曲样式
                    songAdapter.resetSongState();
                    // 更新新播放歌曲的样式
                    Song item = (Song) temp;
                    item.getController().setPlaying(true);
                    songAdapter.setPlayingSongPos(position);
                    // 播放
                    startPlay();
                } else {
                    // 重新播放
                    songAdapter.setPlayingSongPos(position);
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
    private void judgePermission(){
        RxPermissions permissions = new RxPermissions(this);
        // 判断是否已有权限
        boolean isGranted = permissions.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE);
        if(isGranted){
            // 已通过授权
            getLocalMusic();
        } else {
            boolean isRevoked = permissions.isRevoked(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (!isRevoked) {
                // 未申请
                addDisposable( permissions
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
    private void showPermissionTip(){
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
    private void getLocalMusic(){
        addDisposable(
                Flowable.fromCallable(new Callable<List<Song>>() {
                    @Override
                    public List<Song> call(){
                        return MusicHelper.build().getLocalMusic(getActivity());
                    }
                }).subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Song>>() {
                    @Override
                    public void accept(List<Song> songs){
                        if(ObjectHelper.requireNonNull(songs)){
                            viewDelegate.getErrorView().hide();

                            if(songAdapter != null){
                                songAdapter.setNewData(songs);
                                return;
                            }

                            songAdapter = new SongListAdapter(songs){
                                @Override
                                protected void asyncCover(final ImageView view,final Song item) {
                                    addDisposable(
                                            Flowable.fromCallable(new Callable<Bitmap>() {
                                                @Override
                                                public Bitmap call(){
                                                    return MusicHelper.build().getAlbumArt(item.getUrl(),8);
                                                }
                                            }).subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(new Consumer<Bitmap>() {
                                                        @Override
                                                        public void accept(Bitmap bitmap){
                                                            view.setImageBitmap(bitmap);
                                                        }
                                                    })
                                    );
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
     * 开始播放
     */
    private void startPlay(){
        if(songAdapter != null){
            songAdapter.startProgressTimer(0);
        }
    }

    /**
     * 重新播放
     */
    private void replay(){
        if(songAdapter != null){
            songAdapter.startProgressTimer(0);
        }
    }

}
