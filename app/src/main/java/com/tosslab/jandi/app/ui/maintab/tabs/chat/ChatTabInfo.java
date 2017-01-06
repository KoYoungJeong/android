package com.tosslab.jandi.app.ui.maintab.tabs.chat;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.tabs.MainTabInfo;
import com.tosslab.jandi.app.views.TabView;

/**
 * Created by tonyjs on 2016. 8. 18..
 */
public class ChatTabInfo implements MainTabInfo {

    public static final int INDEX = 1;
    private long selectedEntity;

    public ChatTabInfo(long selectedEntity) {
        this.selectedEntity = selectedEntity;
    }

    @Override
    public int getIndex() {
        return INDEX;
    }

    @Override
    public TabView getTabView(LayoutInflater inflater, ViewGroup parent) {
        return (TabView) inflater.inflate(R.layout.tab_chat, parent, false);
    }

    @Override
    public String getTitle() {
        return JandiApplication.getContext()
                .getResources().getString(R.string.jandi_one_to_one);
    }

    @Override
    public Fragment getFragment() {
        return MainChatListFragment.create(selectedEntity);

    }
}
