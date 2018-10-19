package com.lbrong.rumusic.common.adapter;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.bean.Song;
import com.lbrong.rumusic.common.utils.ObjectHelper;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author lbRoNG
 * @since 2018/10/19
 */
public class SongListAdapter extends BaseQuickAdapter<Song,BaseViewHolder> {

    public SongListAdapter(@Nullable List<Song> data) {
        super(R.layout.item_base_song,data);
    }

    protected void asyncCover(ImageView view,Song item){

    }

    @Override
    protected void convert(BaseViewHolder helper, Song item) {
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
        helper.setText(R.id.tv_song_name,item.getTitle());
        helper.setText(R.id.tv_singer,item.getArtist());
    }
}
