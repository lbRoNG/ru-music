package com.lbrong.rumusic.presenter.home;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.lbrong.rumusic.R;
import com.lbrong.rumusic.presenter.base.ActivityPresenter;
import com.lbrong.rumusic.view.home.MainDelegate;

public class MainActivity
       extends ActivityPresenter<MainDelegate>
       implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected Class<MainDelegate> getDelegateClass() {
        return MainDelegate.class;
    }

    @Override
    protected void bindEvenListener() {
        super.bindEvenListener();
        viewDelegate.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void init() {
        super.init();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = viewDelegate.get(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_search:

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_play:
                break;
            case R.id.nav_cache:
                break;
            case R.id.nav_download:
                break;
            case R.id.nav_setting:
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_about:
                break;
        }

        DrawerLayout drawer = viewDelegate.get(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
