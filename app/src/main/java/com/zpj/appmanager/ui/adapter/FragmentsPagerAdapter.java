package com.zpj.appmanager.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class FragmentsPagerAdapter extends FragmentPagerAdapter {

    private final List<? extends Fragment> fragments;
    private final String[] tabTitle;

    public FragmentsPagerAdapter(FragmentManager fm, List<? extends Fragment> fragments, String[] tabTiltle) {
        super(fm);
        this.fragments = fragments;
        this.tabTitle = tabTiltle;
    }

    @Override
    public Fragment getItem(int position) {
        return  fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitle == null ? "" : tabTitle[position];
    }

}
