package com.lbrong.rumusic.view.mine;

import android.support.v7.widget.LinearLayoutManager;
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

    /**
     * 设置适配器
     */
    public void setSongListAdapter(BaseQuickAdapter adapter){
        RecyclerView view = get(R.id.rv_song_list);
        view.setAdapter(adapter);
    }

    /**
     * 设置滚动监听
     */
    public void setOnListScrollListener(RecyclerView.OnScrollListener listener){
        RecyclerView view = get(R.id.rv_song_list);
        view.addOnScrollListener(listener);
    }

    /**
     * 获取布局管理器
     */
    public LinearLayoutManager getLayoutManager(){
        RecyclerView view = get(R.id.rv_song_list);
        return (LinearLayoutManager) view.getLayoutManager();
    }

}
