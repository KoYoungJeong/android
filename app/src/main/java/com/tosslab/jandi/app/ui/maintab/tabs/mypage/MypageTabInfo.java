package com.tosslab.jandi.app.ui.maintab.tabs.mypage;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.tabs.TabInfo;
import com.tosslab.jandi.app.views.TabView;

/**
 * Created by tonyjs on 2016. 8. 18..
 */
public class MypageTabInfo implements TabInfo {

    public static final int INDEX = 4;

    @Override
    public int getIndex() {
        return INDEX;
    }

    @Override
    public TabView getTabView(LayoutInflater inflater, ViewGroup parent) {
        return (TabView) inflater.inflate(R.layout.tab_mypage, parent, false);
    }

    @Override
    public String getTitle() {
        return JandiApplication.getContext()
                .getResources().getString(R.string.jandi_mypage);
    }

    @Override
    public Fragment getFragment() {
        return new MyPageFragment();
    }
}
