package com.tosslab.jandi.app.ui.maintab;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.chat.MainChatListFragment_;
import com.tosslab.jandi.app.ui.maintab.file.FileListFragment;
import com.tosslab.jandi.app.ui.maintab.mypage.MyPageFragment;
import com.tosslab.jandi.app.ui.maintab.team.TeamFragment;
import com.tosslab.jandi.app.ui.maintab.topic.MainTopicListFragment_;
import com.tosslab.jandi.app.views.PagerSlidingTabStrip;

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

    View[] mTabs;
    Fragment[] fragments;
    private long selectedEntity;

    public MainTabPagerAdapter(FragmentManager fm, View[] tabs, long selectedEntity) {
        super(fm);
        mTabs = tabs;
        this.selectedEntity = selectedEntity;
        fragments = new Fragment[mTabs.length];

        initFragments(fragments);
    }

    private void initFragments(Fragment[] fragments) {
        for (int idx = 0; idx < fragments.length; idx++) {
            fragments[idx] = createFragment(idx);
        }
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    private Fragment createFragment(int position) {
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
        if (tvBadge == null) {
            return;
        }
        if (count <= 0) {
            hideBadge(position);
        } else if (count < 1000) {
            tvBadge.setText((String.valueOf(count)));
            showBadge(position);
        } else {
            tvBadge.setText(String.valueOf(999));
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
        } else if (position == TAB_MYPAGE) {
            return tabView.findViewById(R.id.tab_badge_mypage_new);
        }
        return null;
    }

    public void setSelectedEntity(int selectedEntity) {
        this.selectedEntity = selectedEntity;
    }

    public void updateMyPageBadge(int badgeCount) {
        TextView tvMyPage = (TextView) getBadgeView(TAB_MYPAGE);
        updateBadgeText(badgeCount, tvMyPage, TAB_MYPAGE);
    }

    public void onPageSelected(int position) {

        for (int idx = 0; idx < fragments.length; idx++) {

            if (fragments[idx] instanceof OnItemFocused) {
                ((OnItemFocused) fragments[idx]).onItemFocused(idx == position);
            }
        }

    }

    public interface OnItemFocused {
        void onItemFocused(boolean focused);
    }
}
