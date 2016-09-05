package com.tosslab.jandi.app.ui.maintab.tabs.team;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestInviteMemberEvent;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.maintab.tabs.team.adapter.TeamViewPagerAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.info.TeamInfoActivity;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class TeamMainFragment extends Fragment {

    @Bind(R.id.tabs_team_main)
    TabLayout tabLayout;

    @Bind(R.id.viewpager_team_main)
    ViewPager viewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_main, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(new TeamViewPagerAdapter(getActivity(), getChildFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.team_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_team_add:
                EventBus.getDefault().post(new RequestInviteMemberEvent(InvitationDialogExecutor.FROM_MAIN_TEAM));
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamTab, AnalyticsValue.Action.InviteMember);
                break;
            case R.id.menu_team_info:
                TeamInfoActivity.start(getActivity());
                break;
            case R.id.menu_team_search:
                startActivity(Henson.with(getActivity())
                        .gotoTeamMemberSearchActivity()
                        .position(viewPager.getCurrentItem())
                        .isSelectMode(false)
                        .build());
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
