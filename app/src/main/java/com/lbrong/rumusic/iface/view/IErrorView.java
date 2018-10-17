package com.lbrong.rumusic.iface.view;

import com.lbrong.rumusic.iface.listener.OnErrorClickReloadListener;

/**
 * @author lbRoNG
 * @since 2018/8/9
 */
public interface IErrorView {
    void show();
    void hide();
    IErrorView setClickReloadListener(OnErrorClickReloadListener listener);
}
