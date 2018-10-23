package com.lbrong.rumusic.view.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.iface.listener.OnPlayControllerClickListener;

/**
 * @author lbRoNG
 * @since 2018/10/22
 * 播放音乐底部控制器
 */
public class PlayControllerView extends FrameLayout {
    private ProgressBar pb;
    private TextView tvSinger, tvSongName;
    private ImageView ivPlay;
    private OnPlayControllerClickListener listener;

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
        tvSinger = findViewById(R.id.tv_singer);
        tvSongName = findViewById(R.id.tv_song_name);
        ivPlay = findViewById(R.id.iv_play);
        // 监听
        ivPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    String tag = ivPlay.getTag().toString();
                    if(TextUtils.equals(tag,"play")){
                        setStyle(0);
                        listener.onAudioPause();
                    } else {
                        setStyle(1);
                        listener.onAudioPlay();
                    }
                }
            }
        });

        findViewById(R.id.iv_next).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onAudioNext();
                }
            }
        });

        findViewById(R.id.iv_go).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onGo();
                }
            }
        });

        findViewById(R.id.root).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onGo();
                }
            }
        });
        // 允许滚动
        tvSongName.setSelected(true);
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

    public PlayControllerView setStyle(int style){
        ivPlay.setImageResource(style == 1 ? R.drawable.ic_pause_controller_btn : R.drawable.ic_play_controller_btn);
        ivPlay.setTag(style == 1 ? "play" : "pause");
        return this;
    }

    public PlayControllerView setOnClickListener(OnPlayControllerClickListener listener){
        this.listener = listener;
        return this;
    }
}
