package com.tosslab.jandi.app.ui.maintab.tabs.util;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.tosslab.jandi.app.ui.maintab.tabs.TabInfo;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tonyjs on 2016. 8. 18..
 */
public class TabSelector implements TabLayout.OnTabSelectedListener {

    private int containerResId;
    private FragmentManager fragmentManager;
    private OnTabFocusedListener onTabFocusedListener;

    public TabSelector(int containerResId, FragmentManager fragmentManager) {
        this.containerResId = containerResId;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (tab.getTag() == null || !(tab.getTag() instanceof TabInfo)) {
            return;
        }

        showFragment(tab.getPosition(), (TabInfo) tab.getTag());
    }

    private void showFragment(int tabIndex, TabInfo tabInfo) {
        Fragment fragment = tabInfo.getFragment();
        String tag = tabInfo.getTitle();
        Fragment f = fragmentManager.findFragmentByTag(tag);
        if (f != null) {
            LogUtil.e("tony", "showFragment(" + tag + ")");
            fragmentManager.beginTransaction()
                    .show(f)
                    .commit();
        } else {
            LogUtil.e("tony", "addFragment(" + tag + ")");
            fragmentManager.beginTransaction()
                    .add(containerResId, fragment, tag)
                    .commit();
        }

        if (onTabFocusedListener != null) {
            onTabFocusedListener.onTabFocused(tabIndex, tag);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        if (tab.getTag() == null || !(tab.getTag() instanceof TabInfo)) {
            return;
        }

        hideFragment((TabInfo) tab.getTag());
    }

    private void hideFragment(TabInfo tabInfo) {
        String tag = tabInfo.getTitle();
        Fragment f = fragmentManager.findFragmentByTag(tag);
        if (f == null) {
            LogUtil.e("tony", "no hideFragment");
            return;
        }

        LogUtil.e("tony", "hideFragment(" + tag + ")");
        fragmentManager.beginTransaction()
                .hide(f)
                .commit();
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    public void setOnTabFocusedListener(OnTabFocusedListener onTabFocusedListener) {
        this.onTabFocusedListener = onTabFocusedListener;
    }

    public interface OnTabFocusedListener {
        void onTabFocused(int tabIndex, String tabTitle);
    }
}
