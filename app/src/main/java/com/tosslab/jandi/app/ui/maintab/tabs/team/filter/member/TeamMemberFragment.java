package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eowise.recyclerview.stickyheaders.StickyHeadersBuilder;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.entities.disabled.view.DisabledEntityChooseActivity;
import com.tosslab.jandi.app.ui.entities.disabled.view.DisabledEntityChooseActivity_;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.TeamMemberAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.TeamMemberDataView;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.TeamMemberHeaderAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.dagger.DaggerTeamMemberComponent;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.dagger.TeamMemberModule;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.presenter.TeamMemberPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.KeywordObservable;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.OnToggledUser;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.TeamMemberSearchActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.ToggledUserView;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;

public class TeamMemberFragment extends Fragment implements TeamMemberPresenter.View, KeywordObservable, OnToggledUser {

    public static final int REQ_DISABLED_MEMBER = 201;
    @Bind(R.id.list_team_member)
    RecyclerView lvMember;

    @Bind(R.id.layout_team_member_search_empty)
    android.view.View vgEmpty;

    @Bind(R.id.tv_team_member_search_empty)
    TextView tvEmpty;

    @Inject
    TeamMemberPresenter presenter;
    @Nullable
    @InjectExtra(TeamMemberSearchActivity.EXTRA_KEY_SELECT_MODE)
    boolean selectMode;

    @Nullable
    @InjectExtra(TeamMemberSearchActivity.EXTRA_KEY_HAS_HEADER)
    boolean hasHeader = true;

    @Nullable
    @InjectExtra(TeamMemberSearchActivity.EXTRA_KEY_ROOM_ID)
    long roomId = -1;

    private TeamMemberDataView teamMemberDataView;
    private ProgressWheel progressWheel;

    public static Fragment create(Context context, boolean selectMode, boolean hasHeader, long roomId) {
        Bundle args = new Bundle();
        args.putBoolean(TeamMemberSearchActivity.EXTRA_KEY_SELECT_MODE, selectMode);
        args.putBoolean(TeamMemberSearchActivity.EXTRA_KEY_HAS_HEADER, hasHeader);
        args.putLong(TeamMemberSearchActivity.EXTRA_KEY_ROOM_ID, roomId);
        return Fragment.instantiate(context, TeamMemberFragment.class.getName(), args);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_member, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Dart.inject(this, getArguments());

        TeamMemberAdapter adapter = new TeamMemberAdapter();
        adapter.setSelectedMode(selectMode && roomId > 0);
        teamMemberDataView = adapter;
        lvMember.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter.setHasHeader(hasHeader);

        if (hasHeader) {
            adapter.setHasStableIds(true);
            lvMember.addItemDecoration(new StickyHeadersBuilder()
                    .setAdapter(adapter)
                    .setRecyclerView(lvMember)
                    .setSticky(true)
                    .setStickyHeadersAdapter(new TeamMemberHeaderAdapter(adapter), false)
                    .build());
        }

        lvMember.setAdapter(adapter);

        DaggerTeamMemberComponent.builder()
                .teamMemberModule(new TeamMemberModule(this, adapter, adapter, selectMode, roomId))
                .build()
                .inject(this);

        presenter.onCreate();

        teamMemberDataView.setOnItemClickListener((view, adapter1, position) -> {

            presenter.onItemClick(position);
        });
    }

    @Override
    public void moveDisabledMembers() {
        DisabledEntityChooseActivity_.intent(this)
                .startForResult(REQ_DISABLED_MEMBER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_DISABLED_MEMBER && resultCode == Activity.RESULT_OK) {
            long userId = data.getLongExtra(DisabledEntityChooseActivity.EXTRA_RESULT, -1);
            if (userId > 0) {
                presenter.onUserSelect(userId);
            } else {
                ColoredToast.showWarning(R.string.err_profile_get_info);
            }
        }
    }

    @Override
    public void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void refreshDataView() {
        teamMemberDataView.refresh();
    }

    @Override
    public void moveProfile(long userId) {
        MemberProfileActivity_.intent(TeamMemberFragment.this)
                .memberId(userId)
                .from(MemberProfileActivity.EXTRA_FROM_TEAM_MEMBER)
                .start();
    }

    @Override
    public void updateToggledUser(int toggledSize) {
        if (getActivity() instanceof ToggledUserView) {
            ((ToggledUserView) getActivity()).toggle(toggledSize);
        }
    }

    @Override
    public void moveDirectMessage(long teamId, long userId, long roomId, long lastLinkId) {
        MessageListV2Activity_.intent(getActivity())
                .teamId(teamId)
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(userId)
                .roomId(roomId)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .lastReadLinkId(lastLinkId)
                .start();
        getActivity().finish();
    }

    @Override
    public void showPrgoress() {
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(getActivity());
        }
        if (!progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void dismissProgress() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void successToInvitation() {
        getActivity().finish();
    }

    @Override
    public void showFailToInvitation() {
        ColoredToast.showWarning(R.string.err_network);
    }

    @Override
    public void showEmptyView(String keyword) {
        vgEmpty.setVisibility(View.VISIBLE);
        String msg = getString(R.string.jandi_has_no_searched_member_333333, keyword);
        tvEmpty.setText(Html.fromHtml(msg));
    }

    @Override
    public void dismissEmptyView() {
        vgEmpty.setVisibility(View.GONE);
    }

    @Override
    public void setKeywordObservable(Observable<String> keywordObservable) {
        keywordObservable.subscribe(text -> {
            if (presenter != null) {
                presenter.onSearchKeyword(text);
            }
        });
    }

    @Override
    public void onAddToggledUser(long[] users) {
        presenter.addToggledUser(users);
    }

    @Override
    public void onAddAllUser() {
        presenter.addToggleOfAll();
    }

    @Override
    public void onUnselectAll() {
        presenter.clearToggle();
    }

    @Override
    public void onInvite() {
        presenter.inviteToggle();
    }
}