package com.lbrong.rumusic.view.play;

import android.graphics.Bitmap;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.type.PlayMethodEnum;
import com.lbrong.rumusic.iface.listener.OnPlayMethodChangeListener;
import com.lbrong.rumusic.view.base.AppDelegate;

/**
 * @author lbRoNG
 * @since 2018/10/23
 */
public class PlayDelegate extends AppDelegate {

    @Override
    public int getRootLayoutId() {
        return R.layout.activity_play;
    }

    @Override
    public void initWidget() {
        super.initWidget();
    }

    public PlayDelegate setPlayListAdapter(BaseQuickAdapter adapter){
        RecyclerView view = get(R.id.rv_list);
        view.setAdapter(adapter);
        return this;
    }

    public PlayDelegate openDrawer(){
        DrawerLayout drawer = get(R.id.drawer_layout);
        drawer.openDrawer(Gravity.END);
        return this;
    }

    public PlayDelegate closeDrawer(){
        DrawerLayout drawer = get(R.id.drawer_layout);
        drawer.closeDrawer(Gravity.END);
        return this;
    }

    public boolean isDrawerShow(){
        DrawerLayout drawer = get(R.id.drawer_layout);
        return drawer.isDrawerOpen(Gravity.END);
    }

    public PlayDelegate setPlayIcon(boolean isPlaying) {
        ImageView view = get(R.id.iv_start);
        int resId = isPlaying ? R.drawable.ic_play_detail_pause : R.drawable.ic_play_detail_start;
        view.setImageResource(resId);
        return this;
    }

    public PlayDelegate setSongListName(String name) {
        TextView view = get(R.id.tv_list_name);
        view.setText(name);
        return this;
    }

    public PlayDelegate setSongCover(Bitmap cover) {
        ImageView view = get(R.id.iv_cover);
        view.setImageBitmap(cover);
        return this;
    }

    public PlayDelegate setSongName(String name) {
        TextView view = get(R.id.tv_song_name);
        view.setText(name);
        return this;
    }

    public PlayDelegate setSinger(String name) {
        TextView view = get(R.id.tv_singer);
        view.setText(name);
        return this;
    }

    public PlayDelegate setTotalDuration(String total) {
        TextView view = get(R.id.tv_song_total_duration);
        view.setText(total);
        return this;
    }

    public PlayDelegate setCurrentDuration(String current) {
        TextView view = get(R.id.tv_song_current_duration);
        view.setText(current);
        return this;
    }

    public PlayDelegate setCurrentProgressBar(int current) {
        ProgressBar view = get(R.id.view_progress);
        view.setProgress(current);
        return this;
    }

    public PlayDelegate setMaxProgressBar(int max) {
        ProgressBar view = get(R.id.view_progress);
        view.setMax(max);
        return this;
    }

    public PlayDelegate setCurrentSeekBar(int current) {
        SeekBar view = get(R.id.skb_song);
        view.setProgress(current);
        return this;
    }

    public PlayDelegate setMaxSeekBar(int max) {
        SeekBar view = get(R.id.skb_song);
        view.setMax(max);
        return this;
    }

    /**
     * 设置进度条监听
     */
    public PlayDelegate setSeekBarListener(SeekBar.OnSeekBarChangeListener listener) {
        SeekBar view = get(R.id.skb_song);
        view.setOnSeekBarChangeListener(listener);
        return this;
    }

    /**
     * 设置指定播放方法
     */
    public PlayDelegate setPlayMethod(PlayMethodEnum method){
        ImageView iv1 = get(R.id.iv_sort_1);
        ImageView iv2 = get(R.id.iv_sort_2);
        ImageView iv3 = get(R.id.iv_sort_3);
        ImageView iv4 = get(R.id.iv_sort_4);

        iv1.setSelected(method == PlayMethodEnum.ORDER);
        iv2.setSelected(method == PlayMethodEnum.RANDOM);
        iv3.setSelected(method == PlayMethodEnum.SINGLE);
        iv4.setSelected(method == PlayMethodEnum.ORDER_LOOP);

        return this;
    }

    /**
     * 设置播放方式切换监听
     * @param listener
     * @return
     */
    public PlayDelegate setOnPlayMethodChangeListener(OnPlayMethodChangeListener listener){
        if(listener != null){
            PlayChangeListener temp = new PlayChangeListener(listener);
            get(R.id.iv_sort_1).setOnClickListener(temp);
            get(R.id.iv_sort_2).setOnClickListener(temp);
            get(R.id.iv_sort_3).setOnClickListener(temp);
            get(R.id.iv_sort_4).setOnClickListener(temp);
        }
        return this;
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
                get(R.id.iv_sort_1).setSelected(v.getId() == R.id.iv_sort_1);
                get(R.id.iv_sort_2).setSelected(v.getId() == R.id.iv_sort_2);
                get(R.id.iv_sort_3).setSelected(v.getId() == R.id.iv_sort_3);
                get(R.id.iv_sort_4).setSelected(v.getId() == R.id.iv_sort_4);
                // 返回正在选中的播放方式
                switch (v.getId()){
                    case R.id.iv_sort_1:
                        listener.onPlayMethodChange(PlayMethodEnum.ORDER);
                        break;
                    case R.id.iv_sort_2:
                        listener.onPlayMethodChange(PlayMethodEnum.RANDOM);
                        break;
                    case R.id.iv_sort_3:
                        listener.onPlayMethodChange(PlayMethodEnum.SINGLE);
                        break;
                    case R.id.iv_sort_4:
                        listener.onPlayMethodChange(PlayMethodEnum.ORDER_LOOP);
                        break;
                }
            }
        }
    }
}
