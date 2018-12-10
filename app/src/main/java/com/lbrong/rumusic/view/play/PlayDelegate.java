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
import com.lbrong.rumusic.common.type.PlayMethodEnum;
import com.lbrong.rumusic.common.utils.DateUtils;
import com.lbrong.rumusic.iface.listener.OnPlayMethodChangeListener;
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
        setPlayIcon(auto);
    }

    /**
     * 设置进度
     * @param total 总时间
     * @param current 当前时间
     */
    public void setMusicDuration(long total,long current){
        SeekBar bar = get(R.id.skb_song);
        TextView tvCurrent = get(R.id.tv_current_duration);
        TextView tvTotal = get(R.id.tv_total_duration);
        tvCurrent.setText(DateUtils.getDateString(current,"mm:ss"));
        tvTotal.setText(DateUtils.getDateString(total,"mm:ss"));
        bar.setMax((int)(total / 1000));
        bar.setProgress((int)(current / 1000));
    }

    /**
     * 开始cd旋转动画
     */
    public void startCDRotateAnim(){
        // 计算旋转圈数
        // 总时间，最大设置60分钟一首
        long total = 60 * 60 * 1000;
        // 一圈毫秒值
        long one = 20000;
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

    /**
     * 切换播放暂停按钮
     */
    public void setPlayIcon(boolean isPlaying) {
        ImageView view = get(R.id.iv_play);
        int resId = isPlaying ? R.drawable.ic_play_pause : R.drawable.ic_play_start;
        view.setImageResource(resId);
    }

    /**
     * 设置进度
     */
    public void setProgress(long current) {
        SeekBar seekBar = get(R.id.skb_song);
        TextView tvCurrent = get(R.id.tv_current_duration);
        seekBar.setProgress((int)(current / 1000));
        tvCurrent.setText(DateUtils.getDateString(current,"mm:ss"));
    }

    /**
     * 设置播放方式
     */
    public void setPlayMethod(PlayMethodEnum method){
        ImageView iv1 = get(R.id.iv_order_loop);
        ImageView iv2 = get(R.id.iv_order_sort);
        ImageView iv3 = get(R.id.iv_order_single);
        ImageView iv4 = get(R.id.iv_order_random);

        iv1.setSelected(method == PlayMethodEnum.ORDER_LOOP);
        iv2.setSelected(method == PlayMethodEnum.ORDER);
        iv3.setSelected(method == PlayMethodEnum.SINGLE);
        iv4.setSelected(method == PlayMethodEnum.RANDOM);
    }

    /**
     * 设置播放方式切换监听
     */
    public void setOnPlayMethodChangeListener(OnPlayMethodChangeListener listener){
        if(listener != null){
            PlayChangeListener temp = new PlayChangeListener(listener);
            get(R.id.iv_order_loop).setOnClickListener(temp);
            get(R.id.iv_order_sort).setOnClickListener(temp);
            get(R.id.iv_order_single).setOnClickListener(temp);
            get(R.id.iv_order_random).setOnClickListener(temp);
        }
    }

    /**
     * 设置进度条监听
     */
    public void setSeekBarListener(SeekBar.OnSeekBarChangeListener listener) {
        SeekBar view = get(R.id.skb_song);
        view.setOnSeekBarChangeListener(listener);
    }

    /**
     * 播放方式切换监听
     */
    private class PlayChangeListener implements View.OnClickListener{
        private OnPlayMethodChangeListener listener;

        PlayChangeListener(OnPlayMethodChangeListener listener){
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            // 已经选中的不做处理
            if(!v.isSelected()){
                // 未选中的把自己设置为selected，其他都为unSelected
                get(R.id.iv_order_sort).setSelected(v.getId() == R.id.iv_order_sort);
                get(R.id.iv_order_random).setSelected(v.getId() == R.id.iv_order_random);
                get(R.id.iv_order_single).setSelected(v.getId() == R.id.iv_order_single);
                get(R.id.iv_order_loop).setSelected(v.getId() == R.id.iv_order_loop);
                // 返回正在选中的播放方式
                switch (v.getId()){
                    case R.id.iv_order_sort:
                        listener.onPlayMethodChange(PlayMethodEnum.ORDER);
                        break;
                    case R.id.iv_order_random:
                        listener.onPlayMethodChange(PlayMethodEnum.RANDOM);
                        break;
                    case R.id.iv_order_single:
                        listener.onPlayMethodChange(PlayMethodEnum.SINGLE);
                        break;
                    case R.id.iv_order_loop:
                        listener.onPlayMethodChange(PlayMethodEnum.ORDER_LOOP);
                        break;
                }
            }
        }
    }
}
