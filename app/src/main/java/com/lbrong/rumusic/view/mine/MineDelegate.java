package com.lbrong.rumusic.view.mine;

import android.support.v7.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.iface.view.IErrorView;
import com.lbrong.rumusic.view.base.AppDelegate;

/**
 * @author lbRoNG
 * @since 2018/10/18
 */
public class MineDelegate extends AppDelegate {
    @Override
    public int getRootLayoutId() {
        return R.layout.fragment_mine;
    }

    @Override
    public IErrorView getErrorView() {
        return get(R.id.err_root);
    }

    public void setSongListAdapter(BaseQuickAdapter adapter){
        RecyclerView view = get(R.id.rv_song_list);
        view.setAdapter(adapter);
    }
}
