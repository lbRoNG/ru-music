package com.lbrong.rumusic.view.mine;

import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.utils.ObjectHelper;
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

    @Override
    public void initWidget() {
        super.initWidget();
        if(ObjectHelper.requireNonNull(getErrorView())){
            getErrorView().show();
        }
    }
}
