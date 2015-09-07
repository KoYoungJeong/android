package com.tosslab.jandi.app.ui.maintab;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.chat.MainChatListFragment_;
import com.tosslab.jandi.app.ui.maintab.file.FileListFragment_;
import com.tosslab.jandi.app.ui.maintab.more.MainMoreFragment_;
import com.tosslab.jandi.app.ui.maintab.topic.MainTopicListFragment_;
import com.tosslab.jandi.app.views.PagerSlidingTabStrip;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class MainTabPagerAdapter extends FragmentPagerAdapter
        implements PagerSlidingTabStrip.ViewTabProvider {
    private static final int TAB_TOPIC = 0;
    private static final int TAB_CHAT = 1;
    private static final int TAB_FILE = 2;
    private static final int TAB_MORE = 3;

    View[] mTabs;
    private int selectedEntity;

    public MainTabPagerAdapter(FragmentManager fm, View[] tabs, int selectedEntity) {
        super(fm);
        mTabs = tabs;
        this.selectedEntity = selectedEntity;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case TAB_TOPIC:
                return MainTopicListFragment_
                        .builder()
                        .selectedEntity(selectedEntity)
                        .build();
            case TAB_CHAT:
                return MainChatListFragment_
                        .builder()
                        .selectedEntity(selectedEntity)
                        .build();
            case TAB_FILE:
                return FileListFragment_
                        .builder()
                        .build();
            case TAB_MORE:
                return MainMoreFragment_
                        .builder()
                        .build();
            default:
                return MainTopicListFragment_.builder().build();
        }
    }

    @Override
    public int getCount() {
        return mTabs.length;
    }

    @Override
    public View getPageView(int position) {
        return mTabs[position];
    }

    public void updateTopicBadge(int count) {
        TextView tvTopicBadge = (TextView) getBadgeView(TAB_TOPIC);
        updateBadgeText(count, tvTopicBadge, TAB_TOPIC);

    }

    public void updateChatBadge(int count) {
        TextView tvChatBadge = (TextView) getBadgeView(TAB_CHAT);
        updateBadgeText(count, tvChatBadge, TAB_CHAT);
    }

    private void updateBadgeText(int count, TextView tvBadge, int position) {
        if (count <= 0) {
            hideBadge(position);
        } else if (count < 1000) {
            tvBadge.setText((String.valueOf(count)));
            showBadge(position);
        } else {
            tvBadge.setText((String.format("%d+", 999)));
            showBadge(position);
        }
    }

    private void showBadge(int position) {
        View badge = getBadgeView(position);
        if (badge != null) {
            badge.setVisibility(View.VISIBLE);
        }
    }

    private void hideBadge(int position) {
        View badge = getBadgeView(position);
        if (badge != null) {
            badge.setVisibility(View.INVISIBLE);
        }
    }

    private View getBadgeView(int position) {
        View tabView = getPageView(position);
        if (position == TAB_TOPIC) {
            return tabView.findViewById(R.id.tab_badge_topic_new);
        } else if (position == TAB_CHAT) {
            return tabView.findViewById(R.id.tab_badge_chat_new);
        } else if (position == TAB_MORE) {
            return tabView.findViewById(R.id.tab_badge_more_new);
        }
        return null;
    }

    public void setSelectedEntity(int selectedEntity) {
        this.selectedEntity = selectedEntity;
    }

    public void showMoreNewBadge() {
        showBadge(TAB_MORE);
    }

    public void hideMoreNewBadge() {
        hideBadge(TAB_MORE);
    }
}
