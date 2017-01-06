package com.tosslab.jandi.app.ui.maintab;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.tabs.MainTabInfo;

import java.util.List;

import rx.Observable;

public class MainTabPagerAdapter extends FragmentPagerAdapter {

    private SparseArray<String> titles;
    private SparseArray<Fragment> fragments;

    public MainTabPagerAdapter(FragmentManager supportFragmentManager, List<MainTabInfo> tabInfos) {
        super(supportFragmentManager);

        fragments = new SparseArray<>();
        titles = new SparseArray<>();

        Observable.from(tabInfos)
                .subscribe(tabInfo -> {
                    int index = tabInfo.getIndex();
                    titles.put(index, tabInfo.getTitle());

                    Fragment frag = supportFragmentManager.findFragmentByTag(makeFragmentName(R.id.page_main_tab, index));
                    if (frag != null) {
                        fragments.put(index, frag);
                    } else {
                        fragments.put(index, tabInfo.getFragment());
                    }
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

    private static String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }

}
