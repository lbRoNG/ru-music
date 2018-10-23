package com.lbrong.rumusic.view.home;

import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.adapter.TitleFragmentPagerAdapter;
import com.lbrong.rumusic.common.utils.ObjectHelper;
import com.lbrong.rumusic.iface.listener.OnPlayControllerClickListener;
import com.lbrong.rumusic.presenter.home.MainActivity;
import com.lbrong.rumusic.presenter.mine.MineFragment;
import com.lbrong.rumusic.view.base.AppDelegate;
import com.lbrong.rumusic.view.widget.PlayControllerView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lbRoNG
 * @since 2018/10/18
 */
public class MainDelegate extends AppDelegate {
    private ViewPager pager;

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
        initToolbar();
        initRefresh();
    }

    /**
     * 返回正在选中的page index
     */
    public int getSelectIndex(){
        return pager != null?pager.getCurrentItem():0;
    }

    /**
     * 设置刷新监听
     */
    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener){
        SwipeRefreshLayout swipe = get(R.id.swipe);
        swipe.setOnRefreshListener(listener);
    }

    /**
     * 显示刷新
     */
    public void showRefresh(){
        SwipeRefreshLayout swipe = get(R.id.swipe);
        if(!swipe.isRefreshing()){
            swipe.setRefreshing(true);
        }
    }

    /**
     * 隐藏刷新
     */
    public void hideRefresh(){
        SwipeRefreshLayout swipe = get(R.id.swipe);
        if(swipe.isRefreshing()){
            swipe.setRefreshing(false);
        }
    }

    /**
     * 设置侧滑菜单点击监听
     */
    public void setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener listener){
        NavigationView view = get(R.id.nav_view);
        view.setNavigationItemSelectedListener(listener);
    }

    /**
     * 设置下拉刷新
     */
    private void initRefresh(){
        SwipeRefreshLayout swipe = get(R.id.swipe);
        swipe.setColorSchemeColors(ContextCompat.getColor(getActivity(),R.color.colorAccent));
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
     * 初始化tab
     */
    private void initTab(){
        pager = get(R.id.pager);
        TabLayout tabs = get(R.id.tabs);
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new MineFragment());
        fragments.add(new Fragment());
        fragments.add(new Fragment());
        TitleFragmentPagerAdapter pagerAdapter = new TitleFragmentPagerAdapter(
                ((MainActivity)getActivity()).getSupportFragmentManager(),
                fragments,
                getActivity().getResources().getStringArray(R.array.main_tabs));
        pager.setOffscreenPageLimit(3);
        pager.setAdapter(pagerAdapter);
        tabs.setupWithViewPager(pager);
    }

    /**
     * 初始化控制器
     */
    public void initController(String singer, String songName, int progress,int max){
        PlayControllerView view = get(R.id.play_controller);
        view.setAudio(singer,songName,progress,max);
    }

    public void setControllerProgress(int progress){
        PlayControllerView view = get(R.id.play_controller);
        view.setProgress(progress);
    }

    public void showController(){
        PlayControllerView view = get(R.id.play_controller);
        view.setVisibility(View.VISIBLE);
    }

    public void setControllerStyle(int style){
        PlayControllerView view = get(R.id.play_controller);
        view.setStyle(style);
    }

    public void hideController(){
        PlayControllerView view = get(R.id.play_controller);
        view.setVisibility(View.GONE);
    }

    public void setControllerClickListener(OnPlayControllerClickListener listener){
        PlayControllerView view = get(R.id.play_controller);
        view.setOnClickListener(listener);
    }
}
