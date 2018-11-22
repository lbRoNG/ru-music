package com.lbrong.rumusic.view.home;

import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.utils.ObjectHelper;
import com.lbrong.rumusic.iface.listener.OnPlayControllerClickListener;
import com.lbrong.rumusic.iface.view.IErrorView;
import com.lbrong.rumusic.view.base.AppDelegate;
import com.lbrong.rumusic.view.widget.PlayController;

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
     * 设置歌单列表
     */
    public void setSongListAdapter(BaseQuickAdapter adapter){
        RecyclerView view = get(R.id.rv_song_list);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        view.setLayoutManager(manager);
        view.setAdapter(adapter);
    }

    /**
     * 设置歌曲列表
     */
    public void setSongsAdapter(BaseQuickAdapter adapter){
        RecyclerView view = get(R.id.rv_list);
        view.setAdapter(adapter);
    }

    /**
     * 设置滚动监听
     */
    public void setOnListScrollListener(RecyclerView.OnScrollListener listener){
        RecyclerView view = get(R.id.rv_list);
        view.addOnScrollListener(listener);
    }

    /**
     * 获取布局管理器
     */
    public LinearLayoutManager getLayoutManager(){
        RecyclerView view = get(R.id.rv_list);
        return (LinearLayoutManager) view.getLayoutManager();
    }

    /**
     * 设置控制器
     */
    public void setController(byte[] cover,String name,String artist,long duration,long current,boolean auto){
        PlayController controller = get(R.id.play_controller);
        controller.setSongInfo(cover,name,artist,duration,current,auto);
    }

    /**
     * 显示控制器
     */
    public void showController(){
        PlayController controller = get(R.id.play_controller);
        float old = getActivity().getResources().getDimension(R.dimen.play_controller_h);
        if(old == controller.getTranslationY()){
            controller.animate().translationY(1).setDuration(300).start();
        }
    }

    /**f
     * 显示控制器
     */
    public void hideController(){
        PlayController controller = get(R.id.play_controller);
        if(1 == (int) controller.getTranslationY()){
            float old = getActivity().getResources().getDimension(R.dimen.play_controller_h);
            controller.animate().translationY(old).setDuration(300).start();
        }
    }

    /**
     * 设置监听
     */
    public void setControllerCallback(OnPlayControllerClickListener listener){
        PlayController controller = get(R.id.play_controller);
        controller.setCallback(listener);
    }

    /**
     * 暂停控制器
     */
    public void pauseController(){
        PlayController controller = get(R.id.play_controller);
        controller.pauseController();
    }

    /**
     * 继续控制器
     */
    public void resumeController(long current){
        PlayController controller = get(R.id.play_controller);
        controller.resumeController(current);
    }

    /**
     * 设置播放或暂停按钮
     */
    public void setPlayBtn(boolean playing){
        PlayController controller = get(R.id.play_controller);
        controller.setPlayBtn(playing);
    }

    /**
     * 设置进度
     */
    public void setProgress(long current){
        PlayController controller = get(R.id.play_controller);
        controller.setProgress(current);
    }
}
