package com.lbrong.rumusic.common.adapter;

import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.db.table.Song;
import com.lbrong.rumusic.common.utils.DateUtils;
import com.lbrong.rumusic.common.utils.ObjectHelper;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author lbRoNG
 * @since 2018/10/19
 * 首页歌曲列表适配器
 */
public class BasicSongsAdapter extends BaseQuickAdapter<Song,BaseViewHolder> {
    private Song playing;
    private Disposable timer;

    public BasicSongsAdapter(@Nullable List<Song> data) {
        super(R.layout.item_basic_song,data);
    }

    protected void asyncCover(ImageView view,Song item){}

    @Override
    protected void convert(BaseViewHolder helper,final Song item) {
        boolean isPlaying = playing != null && item.getSongId() == playing.getSongId();
        // cover
        byte[] cover = item.getCover();
        if(ObjectHelper.requireNonNull(cover)){
            Glide.with(mContext)
                    .load(cover)
                    .into((ImageView) helper.getView(R.id.iv_cover));
        } else {
            // 获取封面
            asyncCover((ImageView) helper.getView(R.id.iv_cover),item);
        }
        // info
        helper.setText(R.id.tv_song_name,item.getTitle());
        helper.setText(R.id.tv_artist,item.getArtist());
        helper.setText(R.id.tv_duration,DateUtils.getDateString(item.getDuration(),"mm:ss"));
        helper.setTextColor(R.id.tv_song_name,ContextCompat.getColor(
                mContext,isPlaying ? R.color.textThemePrimary : R.color.textPrimary));
        helper.setTextColor(R.id.tv_artist,ContextCompat.getColor(
                mContext,isPlaying ? R.color.textThemeSecondary : R.color.textThirdly));
        helper.setTextColor(R.id.tv_duration,ContextCompat.getColor(
                mContext,isPlaying ? R.color.textThemePrimary : R.color.textThirdly));
        helper.setBackgroundColor(R.id.root,ContextCompat.getColor(
                mContext,isPlaying ? R.color.colorWhite : R.color.colorPrimary));
        helper.getView(R.id.root).setElevation(isPlaying ? 1f : 0f);
        // bitrate
        int bitrate = item.getBitrate();
        boolean showBitrate = bitrate >= 320;
        if(showBitrate){
            helper.setImageResource(R.id.iv_tone,bitrate > 350 ? R.drawable.ic_song_sq : R.drawable.ic_song_hq);
        }
        helper.getView(R.id.iv_tone).setVisibility(showBitrate ? View.VISIBLE : View.GONE);
    }

    /**
     * 设置播放实体
     * @param playing 播放的音乐实体
     * @param auto 自动开启计数器
     */
    public void setPlaying(Song playing,boolean auto){
        // 旧的位置
        int oldIndex = getData().indexOf(this.playing);
        // 新的位置
        int newIndex = getData().indexOf(playing);
        // 刷新实体
        this.playing = playing;
        // 刷新UI
        if(oldIndex != -1){
            notifyItemChanged(oldIndex);
        }
        if(newIndex != -1){
            notifyItemChanged(newIndex);
        }
        // 计数器
        if(auto){
            startTimer(0);
        }
    }

    /**
     * 开启时间计数器
     */
    public void startTimer(long start){
        // 停止
        stopTimer();
        // 重新开启
        start = (start / 1000);
        long total = (playing.getDuration() / 1000) - start + 1;
        timer = Observable.intervalRange(start,total,0,1,TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong){
                        setItemDuration(aLong * 1000);
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
     * 正在播放的歌曲在列表中的位置
     */
    public int getPlayingPos(){
        return getData().indexOf(playing);
    }

    /**
     * 设置特定时间
     */
    public void setItemDuration(long progress){
        int index = getData().indexOf(playing);
        View temp = getViewByPosition(index,R.id.tv_duration);
        if(temp instanceof TextView){
            TextView view = (TextView) getViewByPosition(index,R.id.tv_duration);
            if(view != null){
                view.setText(formatProgressTime(progress,playing.getDuration()));
            }
        }
    }

    /**
     * 计时器是否关闭
     */
    public boolean isTimerStop(){
        return timer == null || timer.isDisposed();
    }

    /**
     * 格式化进度时间
     */
    private String formatProgressTime(long current,long total){
        return DateUtils.getDateString(current,"mm:ss")
                +" / " + DateUtils.getDateString(total,"mm:ss");
    }
}
