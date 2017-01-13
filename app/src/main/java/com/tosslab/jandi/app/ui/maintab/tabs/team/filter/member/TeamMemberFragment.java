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
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.team.TeamInfoChangeEvent;
import com.tosslab.jandi.app.events.team.TeamJoinEvent;
import com.tosslab.jandi.app.events.team.TeamLeaveEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.entities.disabled.view.DisabledEntityChooseActivity;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.TeamMemberAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.TeamMemberDataView;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.TeamMemberHeaderAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.dagger.DaggerTeamMemberComponent;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.dagger.TeamMemberModule;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.presenter.TeamMemberPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.KeywordObservable;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.OnSearchModeChangeListener;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.OnToggledUser;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.TeamMemberSearchActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.ToggledUserView;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.utils.AccessLevelUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.ListScroller;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import rx.Observable;

public class TeamMemberFragment extends Fragment implements TeamMemberPresenter.View,
        KeywordObservable, OnToggledUser, ListScroller, OnSearchModeChangeListener {

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

    @Nullable
    @InjectExtra(TeamMemberSearchActivity.EXTRA_FROM)
    int from = TeamMemberSearchActivity.EXTRA_FROM_TEAM_TAB;

    ProgressWheel progressWheel;
    private TeamMemberDataView teamMemberDataView;

    private boolean isInSearchMode = false;

    private AnalyticsValue.Screen screen = AnalyticsValue.Screen.TeamTab;

    public static Fragment create(Context context, boolean selectMode, boolean hasHeader, long roomId, int from) {
        Bundle args = new Bundle();
        args.putBoolean(TeamMemberSearchActivity.EXTRA_KEY_SELECT_MODE, selectMode);
        args.putBoolean(TeamMemberSearchActivity.EXTRA_KEY_HAS_HEADER, hasHeader);
        args.putLong(TeamMemberSearchActivity.EXTRA_KEY_ROOM_ID, roomId);
        args.putInt(TeamMemberSearchActivity.EXTRA_FROM, from);
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
            presenter.onItemClick(position, screen);
        });

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        setListViewScroll();
    }

    private void setListViewScroll() {
        if (getActivity() instanceof MainTabActivity) {
            MainTabActivity activity = (MainTabActivity) getActivity();
            lvMember.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy > 0) {
                        activity.setTabLayoutVisible(false);
                    } else {
                        activity.setTabLayoutVisible(true);
                    }
                }
            });
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            setAnalyticsScreen();
        }
    }

    private void setAnalyticsScreen() {
        if (from == TeamMemberSearchActivity.EXTRA_FROM_TEAM_TAB) {
            if (isInSearchMode) {
                screen = AnalyticsValue.Screen.TeamTabSearch;
            } else {
                screen = AnalyticsValue.Screen.TeamTab;
            }
        } else if (from == TeamMemberSearchActivity.EXTRA_FROM_INVITE_CHAT) {
            if (isInSearchMode) {
                screen = AnalyticsValue.Screen.SelectTeamMemberSearch;
            } else {
                screen = AnalyticsValue.Screen.SelectTeamMember;
            }
        } else if (from == TeamMemberSearchActivity.EXTRA_FROM_INVITE_TOPIC) {
            if (isInSearchMode) {
                screen = AnalyticsValue.Screen.InviteMemberSearch;
            } else {
                screen = AnalyticsValue.Screen.InviteTeamMembers;
            }
        }
    }

    @Override
    public void moveDisabledMembers() {

        startActivityForResult(Henson.with(getActivity())
                .gotoDisabledEntityChooseActivity()
                .build(), REQ_DISABLED_MEMBER);
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

    public void onDestroy() {
        presenter.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    public void onEvent(MemberStarredEvent event) {
        presenter.onRefresh();
    }

    public void onEvent(ProfileChangeEvent event) {
        presenter.onRefresh();
    }

    public void onEvent(TeamJoinEvent event) {
        presenter.onRefresh();
    }

    public void onEvent(TeamLeaveEvent event) {
        presenter.onRefresh();
    }

    public void onEvent(TeamInfoChangeEvent event) {
        presenter.onRefresh();
    }

    @Override
    public void refreshDataView() {
        teamMemberDataView.refresh();
    }

    @Override
    public void moveProfile(long userId) {
        if (AccessLevelUtil.hasAccessLevel(userId)) {
            startActivity(Henson.with(getActivity())
                    .gotoMemberProfileActivity()
                    .memberId(userId)
                    .from(MemberProfileActivity.EXTRA_FROM_TEAM_MEMBER)
                    .build());
        } else {
            AccessLevelUtil.showDialogUnabledAccessLevel(getActivity());
        }
        AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.ChooseMember);
    }

    @Override
    public void updateToggledUser(int toggledSize) {
        if (getActivity() instanceof ToggledUserView) {
            ((ToggledUserView) getActivity()).toggle(toggledSize);
        }
    }

    @Override
    public void moveDirectMessage(long teamId, long userId, long roomId, long lastLinkId) {
        startActivity(Henson.with(getActivity())
                .gotoMessageListV2Activity()
                .teamId(teamId)
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(userId)
                .roomId(roomId)
                .lastReadLinkId(lastLinkId)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

        AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.ChooseMember);
        if (userId == TeamInfoLoader.getInstance().getJandiBot().getId()) {
            AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.ChooseJANDI);
        }
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

    @Override
    public void scrollToTop() {
        if (lvMember.getAdapter().getItemCount() > 0) {
            lvMember.scrollToPosition(0);
        }
    }

    @Override
    public void onSearchModeChange(boolean isInSearchMode) {
        this.isInSearchMode = isInSearchMode;
        setAnalyticsScreen();
    }

    @Override
    public void showToastNotAnyInvitationMembers() {
        ColoredToast.showError(R.string.warn_all_users_are_already_invited);
        getActivity().finish();
    }


}
