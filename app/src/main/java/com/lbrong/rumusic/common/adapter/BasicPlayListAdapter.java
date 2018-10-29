package com.lbrong.rumusic.common.adapter;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.db.table.Song;
import com.lbrong.rumusic.common.utils.ObjectHelper;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author lbRoNG
 * @since 2018/10/29
 * 播放列表适配器
 */
public class BasicPlayListAdapter extends BaseQuickAdapter<Song,BaseViewHolder> {
    private int playingSongPos = -1;

    public BasicPlayListAdapter(@Nullable List<Song> data) {
        super(R.layout.item_basic_play_list,data);
    }

    protected void asyncCover(ImageView view,Song item){}

    /**
     * 更新新的播放item位置
     */
    public void updatePlayingSongPos(Song item) {
        // 复位上一个
        Song last = getData().get(playingSongPos);
        if(last != null){
            last.getController().setPlaying(false);
        }
        notifyItemChanged(playingSongPos);
        // 更新
        this.playingSongPos = getData().indexOf(item);
        if(playingSongPos != -1){
            getData().get(playingSongPos).getController().setPlaying(true);
        }
        // 新的
        notifyItemChanged(playingSongPos);
    }

    @Override
    protected void convert(BaseViewHolder helper, Song item) {
        boolean isPlaying = item.getController().isPlaying();
        if(isPlaying){
            playingSongPos = helper.getAdapterPosition();
        }
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
            asyncCover((ImageView) helper.getView(R.id.iv_cover),item);
        }
        // info
        helper.setText(R.id.tv_num,String.valueOf(helper.getAdapterPosition() + 1));
        helper.setText(R.id.tv_song_name,item.getTitle());
        helper.setText(R.id.tv_singer,item.getArtist());
        helper.setTextColor(R.id.tv_num,ContextCompat.getColor(
                mContext,isPlaying ? R.color.colorAccent : R.color.textPrimary));
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
        // 正在播放
        helper.getView(R.id.iv_playing).setVisibility(isPlaying ? View.VISIBLE : View.GONE);
        // 菜单点击
        helper.addOnClickListener(R.id.iv_more);
    }
}
