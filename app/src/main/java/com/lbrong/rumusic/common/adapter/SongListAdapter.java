package com.lbrong.rumusic.common.adapter;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.db.table.Song;
import com.lbrong.rumusic.common.utils.ObjectHelper;
import java.lang.ref.WeakReference;
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
 * 我的tab播放列表适配器
 */
public class SongListAdapter extends BaseQuickAdapter<Song,BaseViewHolder> {
    // 正在播放的Song在适配器中的位置
    private int playingSongPos = -1;
    private Disposable timer;

    protected SongListAdapter(@Nullable List<Song> data) {
        super(R.layout.item_basics_song,data);
    }

    protected void asyncCover(ImageView view,Song item){}

    protected void seekBarChange(Song item,int progress){}

    public int getPlayingSongPos() {
        return playingSongPos;
    }

    /**
     * 更新新的播放item位置
     */
    public void updatePlayingSongPos(int pos) {
        // 上个
        resetSongState();
        // 更新
        this.playingSongPos = pos;
        if(playingSongPos != -1){
            getData().get(playingSongPos).getController().setPlaying(true);
        }
        // 新的
        notifyItemChanged(playingSongPos);
    }

    /**
     * 更新新的播放item位置
     */
    public void updatePlayingSongPos(Song item) {
        // 上个
        resetSongState();
        // 更新
        this.playingSongPos = getData().indexOf(item);
        if(playingSongPos != -1){
            getData().get(playingSongPos).getController().setPlaying(true);
        }
        // 新的
        notifyItemChanged(playingSongPos);
    }

    /**
     * 复位上一首播放的歌曲位置的item样式
     */
    public void resetSongState(){
        // 有位置信息
        if(playingSongPos != -1){
            Song item = getItem(playingSongPos);
            if(item != null && item.getController().isPlaying()){
                item.getController().setPlaying(false);
                notifyItemChanged(playingSongPos);
            }
        } else {
            // 没有位置信息，遍历集合
            for (int i = 0; i < getData().size(); i++) {
                Song item = getItem(i);
                if(item != null && item.getController().isPlaying()){
                    playingSongPos = i;
                    item.getController().setPlaying(false);
                    notifyItemChanged(playingSongPos);
                }
            }
        }
    }

    /**
     * 清除计数任务
     */
    public void clearProgressTimer(){
        if(timer != null && !timer.isDisposed()){
            timer.dispose();
        }
    }

    /**
     * 设置item的进度
     */
    public void setItemProgress(int progress){
        SeekBar view = (SeekBar) getViewByPosition(playingSongPos,R.id.skb_song);
        ProgressBar view_ = (ProgressBar) getViewByPosition(playingSongPos,R.id.view_progress);
        if(view != null && view_ != null){
            view.setProgress(progress);
            view_.setProgress(progress);
        }
    }

    /**
     * 开始计数器更新进度
     */
    public void startProgressTimer(int start){
        Song playing = null;
        if(playingSongPos != -1){
            Song item = getItem(playingSongPos);
            if(item != null && item.getController().isPlaying()){
                playing = item;
            }
        } else {
            // 没有位置信息，遍历集合
            for (int i = 0; i < getData().size(); i++) {
                Song item = getItem(i);
                if(item != null && item.getController().isPlaying()){
                    playingSongPos = i;
                    playing = item;
                }
            }
        }

        // 清除之前的任务
        clearProgressTimer();

        // 重新开始
        if(playing != null){
            final long max = (playing.getDuration() / 1000 ) + 1;
            timer = Observable.intervalRange(start + 1,max,0,1,TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        View view;
                        ProgressBar view_;
                        @Override
                        public void accept(Long progress){
                            if(view == null){
                                view = getViewByPosition(playingSongPos,R.id.skb_song);
                            }

                            if(view_ == null){
                                view_ = (ProgressBar) getViewByPosition(playingSongPos,R.id.view_progress);
                            }

                            if(view instanceof SeekBar){
                                SeekBar seekBar = (SeekBar) view;
                                seekBar.setProgress(Integer.parseInt(progress+""));
                            }

                            if(view_ != null){
                                view_.setProgress(Integer.parseInt(progress+""));
                            }

                        }
                    });
        }
    }

    @Override
    protected void convert(BaseViewHolder helper,final Song item) {
        boolean isPlaying = item.getController().isPlaying();
        // cover
        WeakReference<Bitmap> wr = item.getBitmap();
        if(ObjectHelper.requireNonNull(wr)){
            Bitmap cover = wr.get();
            if(ObjectHelper.requireNonNull(cover)){
                helper.setImageBitmap(R.id.iv_cover,cover);
            } else {
                // 获取封面
                asyncCover((ImageView) helper.getView(R.id.iv_cover),item);
            }
        } else {
            helper.setImageResource(R.id.iv_cover,R.drawable.ic_mine_song_default_cover);
        }
        // info
        helper.setText(R.id.tv_song_name,item.getTitle());
        helper.setText(R.id.tv_singer,item.getArtist());
        helper.setTextColor(R.id.tv_song_name,ContextCompat.getColor(
                mContext,isPlaying ? R.color.colorAccent : R.color.textPrimary));
        helper.setTextColor(R.id.tv_singer,ContextCompat.getColor(
                mContext,isPlaying ? R.color.colorAccent : R.color.textSecondary));
        // bitrate
        int bitrate = item.getBitrate();
        boolean showBitrate = bitrate >= 320;
        if(showBitrate){
            helper.setImageResource(R.id.iv_bitrate,bitrate > 350 ? R.drawable.ic_mine_song_sq : R.drawable.ic_mine_song_hq);
        }
        helper.getView(R.id.iv_bitrate).setVisibility(showBitrate ? View.VISIBLE : View.GONE);
        // 进度条是否显示
        if(isPlaying){
            playingSongPos = helper.getAdapterPosition();
        }
        helper.getView(R.id.skb_song).setVisibility(isPlaying ? View.VISIBLE : View.GONE);
        helper.getView(R.id.view_progress).setVisibility(isPlaying ? View.VISIBLE : View.GONE);
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams)
                helper.itemView.getLayoutParams();
        layoutParams.height = (int) mContext.getResources().getDimension(
                isPlaying ? R.dimen.song_item_playing_height : R.dimen.song_item_height);
        helper.itemView.setLayoutParams(layoutParams);
        // 设置进度条
        int max = (int) (item.getDuration() / 1000);
        ProgressBar pb = helper.getView(R.id.view_progress);
        pb.setMax(max);
        SeekBar bar = helper.getView(R.id.skb_song);
        bar.setMax(max);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    startProgressTimer(progress);
                    seekBarChange(item,progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        // 菜单点击
        helper.addOnClickListener(R.id.iv_more);
    }
}
