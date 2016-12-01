package com.tosslab.jandi.app.ui.maintab.tabs.topic;

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
public class TopicTabInfo implements TabInfo {

    public static final int INDEX = 0;
    private long selectedEntity;

    public TopicTabInfo(long selectedEntity) {
        this.selectedEntity = selectedEntity;
    }

    @Override
    public int getIndex() {
        return INDEX;
    }

    @Override
    public TabView getTabView(LayoutInflater inflater, ViewGroup parent) {
        return (TabView) inflater.inflate(R.layout.tab_topic, parent, false);
    }

    @Override
    public String getTitle() {
        return JandiApplication.getContext()
                .getResources().getString(R.string.jandi_tab_topic);
    }

    @Override
    public Fragment getFragment() {
        return MainTopicListFragment_.builder()
                .selectedEntity(selectedEntity)
                .build();
    }

}
