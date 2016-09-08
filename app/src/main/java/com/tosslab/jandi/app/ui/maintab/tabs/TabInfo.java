package com.tosslab.jandi.app.ui.maintab.tabs;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.tosslab.jandi.app.views.TabView;

/**
 * Created by tonyjs on 2016. 8. 18..
 */
public interface TabInfo {

    int getIndex();

    TabView getTabView(LayoutInflater inflater, ViewGroup parent);

    String getTitle();

    Fragment getFragment();

}
