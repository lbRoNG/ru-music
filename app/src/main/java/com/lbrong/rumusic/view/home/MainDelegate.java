package com.lbrong.rumusic.view.home;

import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.utils.ObjectHelper;
import com.lbrong.rumusic.iface.view.IErrorView;
import com.lbrong.rumusic.view.base.AppDelegate;

/**
 * @author lbRoNG
 * @since 2018/10/18
 */
public class MainDelegate extends AppDelegate {

    @Override
    public int getRootLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public Toolbar getToolbar() {
        return get(R.id.toolbar);
    }

    @Override
    public int getOptionsMenuId() {
        return R.menu.main;
    }

    @Override
    public IErrorView getErrorView() {
        return get(R.id.error_view);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        initDrawer();
        initToolbar();
    }

    /**
     * 设置侧滑菜单点击监听
     */
    public void setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener listener){
        NavigationView view = get(R.id.nav_view);
        view.setNavigationItemSelectedListener(listener);
    }

    /**
     * 初始化侧滑菜单
     */
    private void initDrawer(){
        DrawerLayout drawer = get(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(), drawer, getToolbar(), R.string.navigation_drawer_open
                , R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * 初始化Toolbar
     */
    private void initToolbar(){
        if(ObjectHelper.requireNonNull(getActivity())
                && getActivity() instanceof AppCompatActivity){
            AppCompatActivity activity = getActivity();
            ActionBar bar = activity.getSupportActionBar();
            if(ObjectHelper.requireNonNull(bar)){
                bar.setDisplayShowTitleEnabled(false);
            }
            getToolbar().setNavigationIcon(R.drawable.ic_home_menu);
        }
    }

    /**
     * 设置歌曲列表
     */
    private void setSongListAdapter(BaseQuickAdapter adapter){
        RecyclerView view = get(R.id.rv_list);
        view.setAdapter(adapter);
    }
}
