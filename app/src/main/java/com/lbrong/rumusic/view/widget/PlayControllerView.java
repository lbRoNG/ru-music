package com.lbrong.rumusic.view.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lbrong.rumusic.R;

/**
 * @author lbRoNG
 * @since 2018/10/22
 * 播放音乐底部控制器
 */
public class PlayControllerView extends FrameLayout {
    private ProgressBar pb;
    private ImageView ivPlay;
    private TextView tvSinger, tvSongName;

    public PlayControllerView(@NonNull Context context) {
        this(context, null);
    }

    public PlayControllerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayControllerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.widget_play_controller, this);
        pb = findViewById(R.id.progress);
        ivPlay = findViewById(R.id.iv_play);
        tvSinger = findViewById(R.id.tv_singer);
        tvSongName = findViewById(R.id.tv_song_name);
    }

    public PlayControllerView setAudio(String singer, String songName, int progress,int max) {
        tvSinger.setText(singer);
        tvSongName.setText(songName);
        pb.setMax(max);
        pb.setProgress(progress);
        return this;
    }

    public PlayControllerView setProgress(int progress){
        pb.setProgress(progress);
        return this;
    }
}
