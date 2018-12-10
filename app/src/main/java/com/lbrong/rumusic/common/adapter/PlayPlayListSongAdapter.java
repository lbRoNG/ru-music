package com.lbrong.rumusic.common.adapter;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.db.table.PlayList;
import com.lbrong.rumusic.common.db.table.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author lbRoNG
 * @since 2018/10/19
 * 首页播放列表适配器
 */
public class PlayPlayListSongAdapter extends BaseQuickAdapter<Song,BaseViewHolder> {
    private PlayList playList;
    private Song playing;
    private Disposable task;

    public PlayPlayListSongAdapter(@NonNull PlayList playList) {
        super(R.layout.item_play_playlist_song,new ArrayList<Song>());
        this.playList = playList;
        task();
    }

    public void stopTask(){
        if(task != null && !task.isDisposed()){
            task.dispose();
        }
    }

    /**
     * 从数据库拿出播放列表对应的歌曲
     */
    private void task(){
        stopTask();

        task = Observable.fromCallable(new Callable<List<Song>>() {
            @Override
            public List<Song> call(){
                return playList.getSongs();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Song>>() {
                    @Override
                    public void accept(List<Song> songs){
                        setNewData(songs);
                        for (Song s : playList.getSongs()) {
                            if(s.getSongId() == playList.getPlayingId()){
                                playing = s;
                                break;
                            }
                        }
                    }
                });
    }

    @Override
    protected void convert(BaseViewHolder helper,final Song item) {
        boolean isPlaying = playList.getPlayingId() == item.getSongId();
        helper.setText(R.id.tv_num,String.valueOf(helper.getAdapterPosition() + 1));
        helper.setText(R.id.tv_song_name,item.getTitle());
        helper.setText(R.id.tv_artist,item.getArtist());
        helper.setTextColor(R.id.tv_song_name,ContextCompat.getColor(
                mContext,isPlaying ? R.color.textThemePrimary : R.color.textPrimary));
        helper.setTextColor(R.id.tv_artist,ContextCompat.getColor(
                mContext,isPlaying ? R.color.textThemeSecondary : R.color.textThirdly));
        helper.setBackgroundColor(R.id.root,ContextCompat.getColor(
                mContext,isPlaying ? R.color.colorWhite : R.color.colorPrimary));
        helper.getView(R.id.root).setElevation(isPlaying ? 1f : 0f);
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
     */
    public void setPlaying(Song playing){
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
    }

    /**
     * 获取播放中音乐在列表中的位置
     */
    public int getPlayingPos(){
        return getData().indexOf(playing);
    }
}
