package com.lbrong.rumusic.common.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.List;

public class TitleFragmentPagerAdapter extends RefreshFragmentPageAdapter {
    private String[] title;

    public TitleFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragmentList, String[] title) {
        super(fm,fragmentList);
        this.title = title;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }

}
