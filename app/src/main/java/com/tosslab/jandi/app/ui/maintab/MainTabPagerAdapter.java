package com.tosslab.jandi.app.ui.maintab;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;

import com.tosslab.jandi.app.ui.maintab.tabs.TabInfo;

import java.util.List;

import rx.Observable;

public class MainTabPagerAdapter extends FragmentPagerAdapter {

    private SparseArray<String> titles;
    private SparseArray<Fragment> fragments;

    public MainTabPagerAdapter(FragmentManager supportFragmentManager, List<TabInfo> tabInfos) {
        super(supportFragmentManager);

        fragments = new SparseArray<>();
        titles = new SparseArray<>();

        Observable.from(tabInfos)
                .subscribe(tabInfo -> {
                    int index = tabInfo.getIndex();
                    titles.put(index, tabInfo.getTitle());
                    fragments.put(index, tabInfo.getFragment());
                });
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

}
