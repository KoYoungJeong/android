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
import com.tosslab.jandi.app.events.messages.SocketPollEvent;
import com.tosslab.jandi.app.events.poll.RefreshPollBadgeCountEvent;
import com.tosslab.jandi.app.events.poll.RequestRefreshPollBadgeCountEvent;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.component.DaggerMyPageComponent;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.module.MyPageModule;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.presenter.MyPagePresenter;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.views.listeners.ListScroller;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 16. 3. 17..
 */
public class MyPageFragment extends Fragment implements MyPagePresenter.View {

    @Bind(R.id.pager_mypage)
    ViewPager viewPager;
    @Bind(R.id.tab_mypage)
    TabLayout tabLayout;

    @Inject
    MyPagePresenter presenter;

    private TextView tvPollBadge;
    private MyPagePagerAdapter tabPagerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mypage, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        injectComponent();

        ButterKnife.bind(this, view);

        viewPager.setOffscreenPageLimit(2);
        tabPagerAdapter = new MyPagePagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(tabPagerAdapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
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
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                super.onTabReselected(tab);

                Fragment fragment = getFragment(tab.getPosition());
                if (fragment != null && fragment instanceof ListScroller) {
                    ((ListScroller) fragment).scrollToTop();
                }
            }

            @Nullable
            private Fragment getFragment(int position) {
                try {
                    Object item = tabPagerAdapter.instantiateItem(viewPager, position);
                    if (item != null && item instanceof Fragment) {
                        return (Fragment) item;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });

        LayoutInflater inflater = LayoutInflater.from(getContext());
        tabLayout.addTab(tabLayout.newTab()
                .setCustomView(inflater.inflate(R.layout.tab_mypage_mention, tabLayout, false)));
        tabLayout.addTab(tabLayout.newTab()
                .setCustomView(inflater.inflate(R.layout.tab_mypage_star, tabLayout, false)));

        View pollTab = inflater.inflate(R.layout.tab_mypage_poll, tabLayout, false);
        tvPollBadge = (TextView) pollTab.findViewById(R.id.tv_badge);
        tabLayout.addTab(tabLayout.newTab()
                .setCustomView(pollTab));

        viewPager.setCurrentItem(JandiPreference.getLastSelectedTabOfMyPage());

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void injectComponent() {
        DaggerMyPageComponent.builder()
                .myPageModule(new MyPageModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void setPollBadge(int count) {
        if (tvPollBadge == null) {
            return;
        }

        if (count <= 0) {
            tvPollBadge.setVisibility(View.GONE);
            return;
        }

        tvPollBadge.setVisibility(View.VISIBLE);
        tvPollBadge.setText(String.valueOf(Math.min(count, 999)));
    }

    public void onEventMainThread(RefreshPollBadgeCountEvent event) {
        int count = event.getBadgeCount();
        setPollBadge(count);
    }

    public void onEvent(SocketPollEvent event) {
        Poll poll = event.getPoll();
        if (poll == null
                || poll.getTeamId() != AccountRepository.getRepository().getSelectedTeamId()) {
            return;
        }
        presenter.onInitializePollBadge();
    }

    public void onEvent(RequestRefreshPollBadgeCountEvent event) {
        if (event.getTeamId() != AccountRepository.getRepository().getSelectedTeamId()) {
            return;
        }
        presenter.onInitializePollBadge();
    }

    @Override
    public void onResume() {
        super.onResume();

        presenter.onInitializePollBadge();
    }

    @Override
    public void onPause() {
        super.onPause();
        Completable.fromAction(() -> JandiPreference.setLastSelectedTabOfMyPage(viewPager.getCurrentItem()))
                .subscribeOn(Schedulers.computation())
                .subscribe();
    }

    @Override
    public void onDestroyView() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroyView();
    }
}
