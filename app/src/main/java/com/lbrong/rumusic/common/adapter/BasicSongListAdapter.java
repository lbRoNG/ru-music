package com.lbrong.rumusic.common.adapter;

import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.db.table.SongList;
import com.lbrong.rumusic.common.utils.ObjectHelper;

import java.util.List;

/**
 * @author lbRoNG
 * @since 2018/10/19
 * 首页歌单列表适配器
 */
public class BasicSongListAdapter extends BaseQuickAdapter<SongList,BaseViewHolder> {

    public BasicSongListAdapter(@Nullable List<SongList> data) {
        super(R.layout.item_basic_song_list,data);
    }

    protected void asyncCover(ImageView view,SongList item){}

    @Override
    protected void convert(BaseViewHolder helper,final SongList item) {
        // cover
        byte[] cover = item.getCover();
        if(ObjectHelper.requireNonNull(cover)){
            Glide.with(mContext)
                    .load(cover)
                    .into((ImageView) helper.getView(R.id.iv_cover));
        } else {
            // 获取封面
            asyncCover((ImageView) helper.getView(R.id.iv_cover),item);
        }
       helper.setText(R.id.tv_list_name,item.getName());
       helper.setText(R.id.tv_list_count,String.valueOf(item.getCount()) + "首");
    }
}
