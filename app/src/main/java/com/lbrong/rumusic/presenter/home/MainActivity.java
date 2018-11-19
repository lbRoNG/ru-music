package com.lbrong.rumusic.presenter.home;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.db.table.Song;
import com.lbrong.rumusic.common.net.rx.subscriber.DefaultSubscriber;
import com.lbrong.rumusic.common.utils.MusicHelper;
import com.lbrong.rumusic.common.utils.ObjectHelper;
import com.lbrong.rumusic.common.utils.PermissionPageUtils;
import com.lbrong.rumusic.presenter.base.ActivityPresenter;
import com.lbrong.rumusic.view.home.MainDelegate;
import com.lbrong.rumusic.view.widget.ErrorView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.litepal.LitePal;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
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
       implements NavigationView.OnNavigationItemSelectedListener{

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
        // 获取权限
        judgePermission();
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
                                    // 设置

                                } else {
                                    ErrorView errorView = (ErrorView) viewDelegate.getErrorView();
                                    errorView.setText("没有本地音乐哦，快去搜索添加吧！").show();
                                }
                            }
                        })
        );
    }
}
