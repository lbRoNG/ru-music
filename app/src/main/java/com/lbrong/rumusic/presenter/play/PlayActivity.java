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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.adapter.BasicPlayListAdapter;
import com.lbrong.rumusic.common.db.table.Song;
import com.lbrong.rumusic.common.event.EventStringKey;
import com.lbrong.rumusic.common.type.PlayMethodEnum;
import com.lbrong.rumusic.common.utils.DateUtils;
import com.lbrong.rumusic.common.utils.ImageUtils;
import com.lbrong.rumusic.common.utils.MusicHelper;
import com.lbrong.rumusic.common.utils.ObjectHelper;
import com.lbrong.rumusic.common.utils.SendEventUtils;
import com.lbrong.rumusic.common.utils.SettingHelper;
import com.lbrong.rumusic.iface.listener.OnPlayMethodChangeListener;
import com.lbrong.rumusic.presenter.base.ActivityPresenter;
import com.lbrong.rumusic.service.PlayService;
import com.lbrong.rumusic.view.play.PlayDelegate;
import com.lbrong.rumusic.view.widget.ListSwipeHelperCallback;
import org.litepal.LitePal;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
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
       implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, OnPlayMethodChangeListener
        , BaseQuickAdapter.OnItemClickListener,BaseQuickAdapter.OnItemChildClickListener,PopupMenu.OnMenuItemClickListener {

    // 播放服务
    private PlayService playService;
    // 连接引用
    private ServiceConnection serviceConnection;
    // 进去条计时器
    private Disposable progressTimer;
    // 是否列表播放完成
    private boolean isMusicAllComplete;
    // 播放列表拖拽监听
    private ListSwipeHelperCallback<Song> swipeCallback;
    // 播放列表适配器
    private BasicPlayListAdapter playListAdapter;
    // 播放列表显示菜单的item在列表种的位置
    private int playListMenuIndex = -1;

    @Override
    protected Class<PlayDelegate> getDelegateClass() {
        return PlayDelegate.class;
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

    /**
     * 用户改变进度的监听
     */
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
     * 播放列表菜单点击监听
     */
    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        // 记录索引
        playListMenuIndex = position;
        // 显示菜单
        showPlayListMenu(view);
    }

    /**
     * 播放列表点击监听
     */
    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Object obj = adapter.getItem(position);
        if(obj instanceof Song && playService != null){
            Song item = (Song) obj;
            Song playing = playService.getCurrentAudio();
            // 选中播放的音乐和当前播放音乐相同，就不做处理
            if(!item.equals(playing)){
                playService.setAudio(item);
                playService.playAudio();
            }
        }
    }

    /**
     * 菜单选项点击
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (playListMenuIndex != -1 && playService != null){
            // 拿到正在播放的歌曲
            List<Long> ids = playService.getSongListIds();
            long id = ids.get(playListMenuIndex);
            Song song = LitePal.find(Song.class,id);
            // 分析动作
            switch (item.getItemId()){
                // 马上播放
                case R.id.action_play:
                    if(!song.equals(playService.getCurrentAudio())){
                        playService.setAudio(song);
                        playService.playAudio();
                    }
                    break;
                // 下一首播放
                case R.id.action_next:
                    // 删除原来
                    ids.remove(id);
                    // 当前播放歌曲的id
                    long nowId = playService.getCurrentAudio().getId();
                    // 位置
                    int nowIndex = ids.indexOf(nowId);
                    // 添加
                    ids.add(nowIndex + 1,id);
                    // 更新播放列表状态
                    if(playListAdapter != null){
                        List<Song> list = playListAdapter.getData();
                        // 删除
                        list.remove(song);
                        // 添加
                        list.add(nowIndex + 1,song);
                        // 刷新
                        playListAdapter.notifyDataSetChanged();
                    }
                    break;
                // 重复播放
                case R.id.action_repeat:
                    int repeat = song.getRepeat();
                    song.setRepeat(++repeat);
                    song.updateAsync(song.getId());
                    // 如果是正在播放的歌曲
                    if(song.equals(playService.getCurrentAudio())){
                        playService.getCurrentAudio().setRepeat(song.getRepeat());
                    }
                    break;
                // 取消歌曲重复
                case R.id.action_cancel_repeat:
                    song.setRepeat(0);
                    song.updateAsync(song.getId());
                    // 如果是正在播放的歌曲
                    if(song.equals(playService.getCurrentAudio())){
                        playService.getCurrentAudio().setRepeat(0);
                    }
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
                        // 数据库取出对应数据
                        List<Song> songs = new ArrayList<>(ids.size());
                        // 获取对应的歌曲集合,findAll方法顺序不根据id排序，所以只能遍历
                        for (int i = 0; i < ids.size(); i++) {
                            songs.add(LitePal.find(Song.class,ids.get(i)));
                        }
                        return songs;
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Song>>() {
                    @Override
                    public void accept(List<Song> songs){
                        // 设置适配器
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
                        // 点击监听
                        playListAdapter.setOnItemClickListener(PlayActivity.this);
                        playListAdapter.setOnItemChildClickListener(PlayActivity.this);
                        // 支持拖拽操作
                        setPlayListSwipe(songs);
                    }
                })
        );
    }

    /**
     * 设置播放列表拖拽操作
     */
    private void setPlayListSwipe(final List<Song> songs){
        if(swipeCallback == null && ObjectHelper.requireNonNull(songs)){
            swipeCallback = new ListSwipeHelperCallback<Song>(songs) {
                @Override
                public void notifyDismiss(int pos, Song item) {
                    if(playListAdapter != null){
                        // 更新列表
                        playListAdapter.getData().remove(pos);
                        playListAdapter.notifyItemRangeRemoved(pos,1);
                        playListAdapter.notifyItemRangeChanged(pos,songs.size() - pos);
                        // 更新服务
                        if(playService != null){
                            // 正在播放的歌曲，直接下一首
                            if(item.equals(playService.getCurrentAudio())){
                                playService.next(true);
                            }
                            // 更新服务播放列表
                            List<Long> ids = playService.getSongListIds();
                            ids.remove(pos);
                        }
                    }
                }

                @Override
                public void notifyMove(int fromPosition, int toPosition, Song fromItem, Song toItem) {
                    if(playListAdapter != null){
                        // 更新列表
                        Collections.swap(playListAdapter.getData(), fromPosition, toPosition);
                        playListAdapter.notifyItemMoved(fromPosition, toPosition);
                        int start = fromPosition > toPosition ? toPosition : fromPosition;
                        playListAdapter.notifyItemRangeChanged(start,Math.abs(fromPosition - toPosition) + 1);
                        // 更新服务播放列表
                        if(playService != null){
                            List<Long> ids = playService.getSongListIds();
                            Collections.swap(ids, fromPosition, toPosition);
                        }
                    }
                }
            };
            ItemTouchHelper helper = new ItemTouchHelper(swipeCallback);
            helper.attachToRecyclerView((RecyclerView) viewDelegate.get(R.id.rv_list));
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
}
