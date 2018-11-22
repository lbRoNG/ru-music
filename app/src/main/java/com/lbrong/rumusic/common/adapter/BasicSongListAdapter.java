package com.lbrong.rumusic.common.adapter;

import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.widget.ImageView;

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
            helper.setImageBitmap(R.id.iv_cover,BitmapFactory.decodeByteArray(cover,0,cover.length));
        } else {
            // 恢复默认
            helper.setImageResource(R.id.iv_cover,item.getCoverRes());
            // 获取封面
            asyncCover((ImageView) helper.getView(R.id.iv_cover),item);
        }
       helper.setText(R.id.tv_list_name,item.getName());
       helper.setText(R.id.tv_list_count,String.valueOf(item.getCount()) + "首");
    }
}
