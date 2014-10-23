package com.tosslab.jandi.app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.view.View;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.PagerSlidingTabStrip;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class MainTabPagerAdapter extends FragmentPagerAdapter
        implements PagerSlidingTabStrip.ViewTabProvider {
    private static final int TAB_TOPIC  = 0;
    private static final int TAB_CHAT   = 1;
    private static final int TAB_FILE   = 2;
    private static final int TAB_MORE   = 3;

    View[] mTabs;

    public MainTabPagerAdapter(FragmentManager fm, View[] tabs) {
        super(fm);
        mTabs = tabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case TAB_TOPIC:
                return MainPublicListFragment_
                        .builder()
                        .build();
            case TAB_CHAT:
                return MainPrivateListFragment_
                        .builder()
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
                return MainPublicListFragment_.builder().build();
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

    public void showNewTopicBadge() {
        showBadge(TAB_TOPIC);
    }
    public void showNewChatBadge() {
        showBadge(TAB_CHAT);
    }

    private void showBadge(int position) {
        View badge = getBadgeView(position);
        if (badge != null) {
            badge.setVisibility(View.VISIBLE);
        }
    }

    public void hideNewTopicBadge() {
        hideBadge(TAB_TOPIC);
    }
    public void hideNewChatBadge() {
        hideBadge(TAB_CHAT);
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
        }
        return null;
    }
}
