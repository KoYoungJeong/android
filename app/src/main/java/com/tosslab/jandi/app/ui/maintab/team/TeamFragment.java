package com.tosslab.jandi.app.ui.maintab.team;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestInviteMemberEvent;
import com.tosslab.jandi.app.events.team.TeamInfoChangeEvent;
import com.tosslab.jandi.app.events.team.TeamJoinEvent;
import com.tosslab.jandi.app.events.team.TeamLeaveEvent;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.maintab.dialog.UsageInformationDialogFragment_;
import com.tosslab.jandi.app.ui.maintab.team.component.DaggerTeamComponent;
import com.tosslab.jandi.app.ui.maintab.team.module.TeamModule;
import com.tosslab.jandi.app.ui.maintab.team.presenter.TeamPresenter;
import com.tosslab.jandi.app.ui.maintab.team.view.TeamView;
import com.tosslab.jandi.app.ui.maintab.team.vo.Team;
import com.tosslab.jandi.app.ui.members.adapter.searchable.SearchableMemberListAdapter;
import com.tosslab.jandi.app.ui.members.search.MemberSearchActivity;
import com.tosslab.jandi.app.ui.members.view.MemberSearchableDataView;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.utils.KnockListener;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.ListScroller;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public class TeamFragment extends Fragment implements TeamView, ListScroller {

    @Inject
    TeamPresenter presenter;

    @Bind(R.id.lv_team)
    RecyclerView lvTeam;
    @Bind(R.id.progress_team)
    ProgressBar pbTeam;
    @Bind(R.id.tv_team_info_name)
    TextView tvTeamName;
    @Bind(R.id.tv_team_info_domain)
    TextView tvTeamDomain;
    @Bind(R.id.tv_team_info_owner)
    TextView tvTeamOwner;
    @Bind(R.id.vg_team_info)
    View vgTeamInfo;
    @Bind(R.id.vg_team_member_search)
    View vgTeamMemberSearch;

    @Inject
    MemberSearchableDataView memberSearchableDataView;

    private LinearLayoutManager layoutManager;
    private KnockListener usageInformationKnockListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_team, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SearchableMemberListAdapter adapter = new SearchableMemberListAdapter();
        DaggerTeamComponent.builder()
                .teamModule(new TeamModule(this, adapter))
                .build()
                .inject(this);

        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);

        initTeamMemberListView(adapter);

        initUsageInformationKnockListener();

        presenter.onInitializeTeam();
    }

    private void initTeamMemberListView(SearchableMemberListAdapter adapter) {
        layoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        lvTeam.setLayoutManager(layoutManager);
        lvTeam.setAdapter(adapter);
        lvTeam.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                float newTranslateY = vgTeamInfo.getTranslationY() - dy;

                float maxTranslateY =
                        Math.max(-vgTeamInfo.getMeasuredHeight(), newTranslateY);
                float futureTranslateY = Math.min(0, maxTranslateY);

                vgTeamInfo.setTranslationY(futureTranslateY);
                vgTeamMemberSearch.setTranslationY(futureTranslateY);

            }
        });

        memberSearchableDataView.setOnMemberClickListener(member -> {
            showUserProfile(member.getId());
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamTab, AnalyticsValue.Action.SelectMember);
        });
    }

    private void showUserProfile(long userId) {
        MemberProfileActivity_.intent(getActivity())
                .memberId(userId)
                .start();
    }

    private void initUsageInformationKnockListener() {
        usageInformationKnockListener = KnockListener.create()
                .expectKnockCount(10)
                .expectKnockedIn(5000)
                .onKnocked(this::showBugReportDialog);
    }

    @OnClick(R.id.vg_team_info)
    void onClickTeamInfo() {
        usageInformationKnockListener.knock();
    }

    private void showBugReportDialog() {
        UsageInformationDialogFragment_.builder().build()
                .show(getFragmentManager(), "usageInformationKnock");
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
    }

    @OnClick(R.id.vg_team_member_search)
    void moveToSearch() {
        FragmentActivity activity = getActivity();
        ActivityOptionsCompat activityOptions =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                        vgTeamMemberSearch, activity.getString(R.string.jandi_action_search));
        MemberSearchActivity.start(activity, activityOptions);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamTab, AnalyticsValue.Action.MemberSearch);
    }

    @OnClick(R.id.btn_team_info_invite)
    void inviteMember() {
        EventBus.getDefault().post(new RequestInviteMemberEvent(InvitationDialogExecutor.FROM_MAIN_TEAM));
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamTab, AnalyticsValue.Action.InviteMember);
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void showProgress() {
        pbTeam.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        pbTeam.setVisibility(View.GONE);
    }

    public void onEvent(TeamLeaveEvent event) {
        presenter.reInitializeTeam();
    }

    public void onEvent(TeamInfoChangeEvent event) {
        presenter.reInitializeTeam();
    }

    public void onEvent(TeamJoinEvent event) {
        presenter.reInitializeTeam();
    }

    @Override
    public void initTeamInfo(Team team) {
        tvTeamName.setText(team.getName());
        tvTeamDomain.setText(team.getDomain());
        String owner = JandiApplication.getContext()
                .getResources()
                .getString(R.string.jandi_team_owner_with_format, team.getOwner().getName());
        tvTeamOwner.setText(owner);
    }

    @Override
    public void notifyDataSetChanged() {
        memberSearchableDataView.notifyDataSetChanged();
    }

    @Override
    public void scrollToTop() {
        vgTeamInfo.setTranslationY(0);
        vgTeamMemberSearch.setTranslationY(0);
        lvTeam.scrollToPosition(0);
    }
}
