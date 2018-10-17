package com.lbrong.rumusic.presenter.base;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.MenuItem;

import com.lbrong.rumusic.R;
import com.lbrong.rumusic.view.base.IDelegate;

/**
 * @author lbRoNG
 * @since 2018/7/31
 * 一个activity管理多个fragment
 */
public abstract class BackStackActivityPresenter<T extends IDelegate>
        extends ActivityPresenter<T>{

    public abstract int getFragmentContainerId();

    @Override
    public void onBackPressed() {
        managerBackStack();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            managerBackStack();
        }
        return true;
    }

    public void addFragment(Fragment fragment, String tag){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(
                        R.anim.slide_right_in,
                        R.anim.slide_left_out,
                        R.anim.slide_left_in,
                        R.anim.slide_right_out
                )
                .add(getFragmentContainerId(),fragment,tag)
                .addToBackStack(tag);
        transaction.commit();
    }

    public void removeFragment(String tag){
        if(!TextUtils.isEmpty(tag)){
            FragmentManager manager = getSupportFragmentManager();
            Fragment fragment = manager.findFragmentByTag(tag);
            if(fragment != null){
                manager.popBackStack(tag,0);
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.remove(fragment);
                transaction.commit();
            }
        }
    }

    public void managerBackStack(){
        FragmentManager manager = getSupportFragmentManager();
        int count = manager.getBackStackEntryCount();
        if(count > 1){
            manager.popBackStack();

            int index = count - 2;
            FragmentManager.BackStackEntry entry = manager.getBackStackEntryAt(index);
            String name = entry.getName();
            topStackName(name);
        } else {
            finish();
        }
    }

    public void topStackName(String name){}

    public Fragment getFragmentByTag(String tag){
        FragmentManager manager = getSupportFragmentManager();
        return manager.findFragmentByTag(tag);
    }

}
