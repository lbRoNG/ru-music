package com.lbrong.rumusic.view.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.iface.listener.OnPlayControllerClickListener;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 播放控制台
 */
public class PlayController
       extends FrameLayout
       implements View.OnClickListener {

    private TextView tvSongName,tvArtist;
    private ImageView ivCover;
    private ImageView ivPlay;
    private ProgressBar pbDuration;
    private OnPlayControllerClickListener callback;
    private Disposable timer; // 计时器
    private long duration; // 计时器记录

    public PlayController(@NonNull Context context) {
        this(context,null);
    }

    public PlayController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PlayController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback != null){
                    callback.onGo();
                }
            }
        });
        init();
    }

    private void init(){
        LayoutInflater.from(getContext()).inflate(R.layout.widget_play_controller,this);
        tvSongName = findViewById(R.id.tv_song_name);
        tvArtist = findViewById(R.id.tv_artist);
        ivCover = findViewById(R.id.iv_cover);
        ivPlay = findViewById(R.id.iv_play);
        pbDuration = findViewById(R.id.pb_duration);
        ivPlay.setOnClickListener(this);
        findViewById(R.id.iv_next).setOnClickListener(this);
        findViewById(R.id.iv_list).setOnClickListener(this);
        tvSongName.setSelected(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        stopTimer();
        stopCDRotateAnim();
        super.onDetachedFromWindow();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_play:
                // 状态改变
                String tag = ivPlay.getTag().toString();
                boolean playing = "pause".equals(tag);
                setPlayBtn(!playing);
                // 回调
                if (callback != null){
                    if(playing){
                        callback.onAudioPause();
                    } else {
                        callback.onAudioPlay();
                    }
                }
                break;
            case R.id.iv_next:
                if (callback != null){
                    callback.onAudioNext();
                }
                break;
            case R.id.iv_list:
                if (callback != null){
                    callback.onPlayList();
                }
        }
    }

    /**
     * 设置歌曲信息
     * @param cover 封面
     * @param name 歌名
     * @param artist 歌手
     * @param duration 总长，毫秒
     * @param current 当前，毫秒
     * @param auto 是否开启计时器
     */
    public void setSongInfo(byte[] cover,String name,String artist,long duration,long current,boolean auto){
        Glide.with(this)
                .load(cover != null ? cover : R.drawable.ic_song_circle_default_cover)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(100)))
                .into(ivCover);
        tvSongName.setText(name);
        tvArtist.setText(artist);
        pbDuration.setMax((int)(duration / 1000));
        pbDuration.setProgress((int)(current / 1000));
        this.duration = duration;
        if(auto){
            startTimer(current,duration);
            startCDRotateAnim();
        }
    }

    public void setCallback(OnPlayControllerClickListener listener){
        this.callback = listener;
    }

    /**
     * 设置当前进度
     * @param current 进度，毫秒
     */
    public void setProgress(long current){
        pbDuration.setProgress((int)(current / 1000));
    }

    /**
     * 开启时间计数器
     */
    public void startTimer(long start,long total){
        // 停止
        stopTimer();
        // 设置值
        start = (start / 1000) + 1;
        total = (total / 1000) - start;
        if(start < 0 || total < 0 || start > total){
            return;
        }
        // 开始
        timer = Observable.intervalRange(start,total,0,1,TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong){
                        pbDuration.setProgress(aLong.intValue());
                    }
                });
    }

    /**
     * 停止计数器
     */
    public void stopTimer(){
        if(timer != null && !timer.isDisposed()){
            timer.dispose();
        }
    }

    /**
     * 开始cd旋转动画
     */
    public void startCDRotateAnim(){
        // 计算旋转圈数
        // 总时间，最大设置60分钟一首
        long total = 60 * 60 * 1000;
        // 一圈毫秒值
        long one = 12000;
        // 圈数
        long count = total / one;
        // 取消原有动画
        stopCDRotateAnim();
        // 开启动画
        ivCover.animate()
                .setStartDelay(0)
                .setInterpolator(new LinearInterpolator())
                .rotation(count * 360f)
                .setDuration(total);
    }

    /**
     * 停止cd旋转动画
     */
    public void stopCDRotateAnim(){
        ivCover.animate().cancel();
    }

    /**
     * 暂停控制器
     */
    public void pauseController(){
        stopCDRotateAnim();
        stopTimer();
    }

    /**
     * 继续控制器
     */
    public void resumeController(long current){
        startCDRotateAnim();
        startTimer(current,duration);
    }

    /**
     * 设置播放或暂停按钮
     */
    public void setPlayBtn(boolean playing){
        ivPlay.setTag(playing ? "pause" : "play");
        ivPlay.setImageResource(playing ? R.drawable.ic_controller_pause : R.drawable.ic_controller_play);
    }
}
