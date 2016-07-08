package com.tosslab.jandi.app.ui.maintab;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.ui.maintab.chat.MainChatListFragment_;
import com.tosslab.jandi.app.ui.maintab.file.FileListFragment;
import com.tosslab.jandi.app.ui.maintab.mypage.MyPageFragment;
import com.tosslab.jandi.app.ui.maintab.team.TeamFragment;
import com.tosslab.jandi.app.ui.maintab.topic.MainTopicListFragment_;
import com.tosslab.jandi.app.views.PagerSlidingTabStrip;
import com.tosslab.jandi.app.views.TabView;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class MainTabPagerAdapter extends FragmentPagerAdapter
        implements PagerSlidingTabStrip.ViewTabProvider {

    public static final int TAB_TOPIC = 0;
    public static final int TAB_CHAT = 1;
    public static final int TAB_FILE = 2;
    public static final int TAB_TEAM = 3;
    public static final int TAB_MYPAGE = 4;

    private TabView[] tabViews;
    private long selectedEntity;

    public MainTabPagerAdapter(FragmentManager fm, TabView[] tabs, long selectedEntity) {
        super(fm);
        this.tabViews = tabs;
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
                return new FileListFragment();
            case TAB_TEAM:
                return new TeamFragment();
            case TAB_MYPAGE:
                return new MyPageFragment();
            default:
                return MainTopicListFragment_.builder().build();
        }
    }

    @Override
    public int getCount() {
        return tabViews.length;
    }

    @Override
    public View getPageView(int position) {
        return tabViews[position];
    }

    public void updateTopicBadge(int count) {
        TabView tvTopicBadge = getBadgeView(TAB_TOPIC);
        updateBadgeText(count, tvTopicBadge);

    }

    public void updateChatBadge(int count) {
        TabView tvChatBadge = getBadgeView(TAB_CHAT);
        updateBadgeText(count, tvChatBadge);
    }

    public void updateMyPageBadge(int badgeCount) {
        TabView tvMyPage = getBadgeView(TAB_MYPAGE);
        updateBadgeText(badgeCount, tvMyPage);
    }

    private void updateBadgeText(int count, TabView tvTab) {
        if (tvTab == null) {
            return;
        }
        if (count <= 0) {
            tvTab.hideBadge();
        } else if (count < 1000) {
            tvTab.setBadgeText((String.valueOf(count)));
            tvTab.showBadge();
        } else {
            tvTab.setBadgeText(String.valueOf(999));
            tvTab.showBadge();
        }
    }

    @Nullable
    private TabView getBadgeView(int position) {
        if (position == TAB_FILE || position == TAB_TEAM) {
            return null;
        }

        return tabViews[position];
    }

    public void setSelectedEntity(int selectedEntity) {
        this.selectedEntity = selectedEntity;
    }

}
