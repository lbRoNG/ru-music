package com.lbrong.rumusic.view.home;

import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;

import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.adapter.TitleFragmentPagerAdapter;
import com.lbrong.rumusic.presenter.home.MainActivity;
import com.lbrong.rumusic.view.base.AppDelegate;

import java.util.ArrayList;
import java.util.List;

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
    public void initWidget() {
        super.initWidget();
        initDrawer();
        initTab();
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
     * 初始化tab
     */
    private void initTab() {
        ViewPager pager = get(R.id.pager);
        TabLayout tabs = get(R.id.tabs);
        List<Fragment> fragmentPresenters = new ArrayList<>();
        fragmentPresenters.add(new Fragment());
        fragmentPresenters.add(new Fragment());
        fragmentPresenters.add(new Fragment());
        TitleFragmentPagerAdapter pagerAdapter = new TitleFragmentPagerAdapter(
                ((MainActivity)getActivity()).getSupportFragmentManager(),
                fragmentPresenters,
                getActivity().getResources().getStringArray(R.array.main_tabs));
        pager.setOffscreenPageLimit(3);
        pager.setAdapter(pagerAdapter);
        tabs.setupWithViewPager(pager);
    }

}
