package com.lbrong.rumusic.view.play;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lbrong.rumusic.R;
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

    public PlayDelegate setSeekBarListener(SeekBar.OnSeekBarChangeListener listener) {
        SeekBar view = get(R.id.skb_song);
        view.setOnSeekBarChangeListener(listener);
        return this;
    }
}
