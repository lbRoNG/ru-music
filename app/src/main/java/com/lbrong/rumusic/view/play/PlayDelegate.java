package com.lbrong.rumusic.view.play;

import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.utils.DateUtils;
import com.lbrong.rumusic.view.base.AppDelegate;

public class PlayDelegate extends AppDelegate {
    @Override
    public int getRootLayoutId() {
        return R.layout.activity_play;
    }

    @Override
    public void initWidget() {
        super.initWidget();
        initBottomSheet();
    }

    private void initBottomSheet(){
        BottomSheetBehavior behavior = BottomSheetBehavior.from(get(R.id.rv_play_list));
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        behavior.setPeekHeight(0);
        behavior.setHideable(true);
    }

    public void showBottomSheet(){
        RecyclerView view = get(R.id.rv_play_list);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(view);
        if(behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
            view.setVisibility(View.VISIBLE);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    /**
     * 设置歌曲列表
     */
    public void setPlayListSongsAdapter(BaseQuickAdapter adapter) {
        RecyclerView view = get(R.id.rv_play_list);
        view.setAdapter(adapter);
    }

    /**
     * 歌曲信息
     * @param cover 封面
     * @param name 歌名
     * @param artist 歌手
     * @param auto 是否开启动画
     */
    public void setMusicInfo(byte[] cover,String name,String artist,boolean auto){
        TextView tvName = get(R.id.tv_song_name);
        TextView tvArtist = get(R.id.tv_artist);
        tvName.setText(name);
        tvArtist.setText(artist);
        Glide.with(getActivity())
                .load(cover)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(660)))
                .into((ImageView)get(R.id.iv_cover));
        if(auto){
            startCDRotateAnim();
        }
    }

    /**
     * 设置进度
     * @param total 总时间
     * @param current 当前时间
     */
    public void setMusicDuration(long total,long current){
        SeekBar bar = get(R.id.sek_song);
        TextView tvCurrent = get(R.id.tv_current_duration);
        TextView tvTotal = get(R.id.tv_total_duration);
        tvCurrent.setText(DateUtils.getDateString(current,"mm:ss"));
        tvTotal.setText(DateUtils.getDateString(total,"mm:ss"));
        bar.setProgress((int) ((current * 100) / total));
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
        get(R.id.iv_cover).animate()
                .setStartDelay(0)
                .setInterpolator(new LinearInterpolator())
                .rotation(count * 360f)
                .setDuration(total);
    }

    /**
     * 停止cd旋转动画
     */
    public void stopCDRotateAnim(){
        get(R.id.iv_cover).animate().cancel();
    }
}
