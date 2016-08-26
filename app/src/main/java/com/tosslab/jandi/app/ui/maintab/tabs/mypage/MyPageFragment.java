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
import com.tosslab.jandi.app.events.poll.RefreshPollBadgeCountEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.views.TabView;
import com.tosslab.jandi.app.views.listeners.ListScroller;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by tonyjs on 16. 3. 17..
 */
public class MyPageFragment extends Fragment implements ListScroller {

    @Bind(R.id.pager_mypage)
    ViewPager viewPager;
    @Bind(R.id.tab_mypage)
    TabLayout tabLayout;

    private TextView tvPollBadge;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mypage, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        viewPager.setOffscreenPageLimit(3);
        MyPagePagerAdapter adapter = new MyPagePagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.removeAllTabs();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        tabLayout.addTab(tabLayout.newTab()
                .setCustomView(inflater.inflate(R.layout.tab_mypage_mention, tabLayout, false)));
        tabLayout.addTab(tabLayout.newTab()
                .setCustomView(inflater.inflate(R.layout.tab_mypage_star, tabLayout, false)));

        View pollTab = inflater.inflate(R.layout.tab_mypage_poll, tabLayout, false);
        tvPollBadge = (TextView) pollTab.findViewById(R.id.tv_badge);
        setPollBadge(TeamInfoLoader.getInstance().getPollBadge());
        tabLayout.addTab(tabLayout.newTab()
                .setCustomView(pollTab));

        EventBus.getDefault().register(this);
    }

    private void setPollBadge(int count) {
        if (count <= 0) {
            tvPollBadge.setVisibility(View.GONE);
            return;
        }
        tvPollBadge.setVisibility(View.VISIBLE);
        tvPollBadge.setText(Integer.toString(count));
    }

    public void onEventMainThread(RefreshPollBadgeCountEvent event) {
        int count = event.getBadgeCount();
        setPollBadge(count);
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void scrollToTop() {

    }
}
