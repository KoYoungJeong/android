package com.tosslab.jandi.app.ui.maintab.tabs.mypage;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RefreshMentionBadgeCountEvent;
import com.tosslab.jandi.app.events.RefreshMypageBadgeCountEvent;
import com.tosslab.jandi.app.events.messages.SocketPollEvent;
import com.tosslab.jandi.app.events.poll.RequestRefreshPollBadgeCountEvent;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.BaseLazyFragment;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.ListScroller;
import com.tosslab.jandi.app.views.listeners.TabFocusListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.schedulers.Schedulers;

public class MyPageFragment extends BaseLazyFragment implements TabFocusListener {

    @Bind(R.id.pager_mypage)
    ViewPager viewPager;
    @Bind(R.id.tab_mypage)
    TabLayout tabLayout;

    private MyPagePagerAdapter tabPagerAdapter;
    private TextView tvPollbadge;
    private TextView tvMentionBadge;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager.setOffscreenPageLimit(2);
        tabPagerAdapter = new MyPagePagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(tabPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.getTabAt(0).setCustomView(R.layout.tab_mypage_mention);
        tvMentionBadge = (TextView) tabLayout.getTabAt(0).getCustomView().findViewById(R.id.tv_badge);

        tabLayout.getTabAt(1).setCustomView(R.layout.tab_mypage_star);

        tabLayout.getTabAt(2).setCustomView(R.layout.tab_mypage_poll);
        tvPollbadge = (TextView) tabLayout.getTabAt(2).getCustomView().findViewById(R.id.tv_badge);

        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                switch (tab.getPosition()) {
                    case 0:
                        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MypageTab, AnalyticsValue.Action.MentionTab);
                        break;
                    case 1:
                        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MypageTab, AnalyticsValue.Action.StarTab);
                        break;
                    case 2:
                        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.MypageTab, AnalyticsValue.Action.PollTab);
                        break;
                }

                Fragment fragment = getFragment(tab.getPosition());

                if (fragment != null && fragment instanceof TabFocusListener) {
                    ((TabFocusListener) fragment).onFocus();
                }

                setBadges();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                super.onTabReselected(tab);

                Fragment fragment = getFragment(tab.getPosition());
                if (fragment != null && fragment instanceof ListScroller) {
                    ((ListScroller) fragment).scrollToTop();
                }

                if (fragment != null && fragment instanceof TabFocusListener) {
                    ((TabFocusListener) fragment).onFocus();
                }
            }

            @Nullable
            private Fragment getFragment(int position) {
                try {
                    Fragment item = tabPagerAdapter.getItem(position);
                    if (item != null) {
                        return item;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });

        viewPager.setCurrentItem(JandiPreference.getLastSelectedTabOfMyPage());

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        setBadges();

        onFocus();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mypage, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    private void setMentionBadge() {
        int mentionBadgeCnt = TeamInfoLoader.getInstance().getMention().getUnreadCount();
        if (mentionBadgeCnt > 0) {
            tvMentionBadge.setVisibility(View.VISIBLE);
            tvMentionBadge.setText(String.valueOf(Math.min(mentionBadgeCnt, 999)));
        } else {
            tvMentionBadge.setVisibility(View.GONE);
        }
    }

    private void setPollBadge() {
        int pollBadge = TeamInfoLoader.getInstance().getPollBadge();
        if (pollBadge > 0) {
            tvPollbadge.setVisibility(View.VISIBLE);
            tvPollbadge.setText(String.valueOf(Math.min(pollBadge, 999)));
        } else {
            tvPollbadge.setVisibility(View.GONE);
        }
    }

    private void setBadges() {
        setMentionBadge();
        setPollBadge();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.MypageTab);
        }
    }

    public void onEventMainThread(SocketPollEvent event) {
        Poll poll = event.getPoll();
        if (poll == null
                || poll.getTeamId() != AccountRepository.getRepository().getSelectedTeamId()) {
            return;
        }
        setPollBadge();
    }

    public void onEventMainThread(RefreshMentionBadgeCountEvent event) {
        setMentionBadge();
    }

    public void onEventMainThread(RefreshMypageBadgeCountEvent event) {
        setBadges();
    }

    public void onEventMainThread(RequestRefreshPollBadgeCountEvent event) {
        if (event.getTeamId() != AccountRepository.getRepository().getSelectedTeamId()) {
            return;
        }
        setPollBadge();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            setBadges();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getUserVisibleHint()) {
            Completable.fromAction(() -> JandiPreference.setLastSelectedTabOfMyPage(viewPager.getCurrentItem()))
                    .subscribeOn(Schedulers.computation())
                    .subscribe();
        }
    }

    @Override
    public void onDestroyView() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroyView();
    }

    @Override
    public void onFocus() {
        if (tabPagerAdapter != null || viewPager != null) {
            Fragment item = tabPagerAdapter.getItem(viewPager.getCurrentItem());

            if (item != null && item instanceof TabFocusListener) {
                ((TabFocusListener) item).onFocus();
            }
        }
    }

}