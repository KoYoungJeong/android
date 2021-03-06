package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept;


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
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.MemberRankUpdatedEvent;
import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.team.TeamInfoChangeEvent;
import com.tosslab.jandi.app.events.team.TeamJoinEvent;
import com.tosslab.jandi.app.events.team.TeamLeaveEvent;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.adapter.DeptJobAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.adapter.DeptJobDataView;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.adapter.DeptJobHeaderAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.dagger.DaggerDeptJobComponent;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.dagger.DeptJobModule;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.presenter.DeptJobPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.deptgroup.DeptJobGroupActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.KeywordObservable;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.OnSearchModeChangeListener;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.TeamMemberSearchActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.ToggledUserView;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.ListScroller;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import rx.Observable;

public class DeptJobFragment extends Fragment implements DeptJobPresenter.View,
        KeywordObservable, ListScroller, OnSearchModeChangeListener {

    public static final int EXTRA_TYPE_DEPT = 1;
    public static final int EXTRA_TYPE_JOB = 2;
    public static final String EXTRA_TYPE = "type";
    private static final int REQ_MEMBERS_OF_GROUP = 101;

    @Nullable
    @InjectExtra(EXTRA_TYPE)
    int type = EXTRA_TYPE_DEPT;
    @Nullable
    @InjectExtra(TeamMemberSearchActivity.EXTRA_KEY_SELECT_MODE)
    boolean selectMode = false;
    @Nullable
    @InjectExtra(TeamMemberSearchActivity.EXTRA_KEY_HAS_HEADER)
    boolean hasHeader = true;
    @Nullable
    @InjectExtra(TeamMemberSearchActivity.EXTRA_KEY_ROOM_ID)
    long roomId = -1;
    @Nullable
    @InjectExtra(TeamMemberSearchActivity.EXTRA_FROM)
    int from = TeamMemberSearchActivity.EXTRA_FROM_TEAM_TAB;


    @Bind(R.id.list_team_dept_job)
    RecyclerView lvMember;

    @Bind(R.id.layout_team_member_search_empty)
    android.view.View vgEmpty;

    @Bind(R.id.tv_team_member_search_empty)
    TextView tvEmpty;

    @Inject
    DeptJobDataView deptJobDataView;

    @Inject
    DeptJobPresenter deptJobPresenter;

    private boolean isInSearchMode = false;
    private AnalyticsValue.Screen screen = AnalyticsValue.Screen.TeamTab;

    public static Fragment create(Context context, int type, boolean selectMode, boolean hasHeader, long roomId, int from) {
        Bundle args = new Bundle(1);
        args.putInt(EXTRA_TYPE, type);
        args.putBoolean(TeamMemberSearchActivity.EXTRA_KEY_SELECT_MODE, selectMode);
        args.putBoolean(TeamMemberSearchActivity.EXTRA_KEY_HAS_HEADER, hasHeader);
        args.putLong(TeamMemberSearchActivity.EXTRA_KEY_ROOM_ID, roomId);
        args.putInt(TeamMemberSearchActivity.EXTRA_FROM, from);
        return Fragment.instantiate(context, DeptJobFragment.class.getName(), args);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_dept_job, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Dart.inject(this, getArguments());

        DeptJobAdapter adapter = new DeptJobAdapter(selectMode);
        lvMember.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (hasHeader) {
            adapter.setHasStableIds(true);
            lvMember.addItemDecoration(new StickyHeadersBuilder()
                    .setAdapter(adapter)
                    .setRecyclerView(lvMember)
                    .setSticky(true)
                    .setStickyHeadersAdapter(new DeptJobHeaderAdapter(adapter), false)
                    .build());
        }

        lvMember.setAdapter(adapter);
        DaggerDeptJobComponent.builder()
                .deptJobModule(new DeptJobModule(this, adapter, type, selectMode, roomId))
                .build()
                .inject(this);

        deptJobDataView.setOnItemClick((view, adapter1, position) -> {
            deptJobPresenter.onItemClick(position);

            String keyword = ((DeptJobAdapter) adapter1).getItem(position).getName().toString();

            startActivityForResult(Henson.with(getActivity())
                    .gotoDeptJobGroupActivity()
                    .keyword(keyword)
                    .type(type)
                    .selectMode(selectMode)
                    .pickMode(selectMode && roomId < 0)
                    .from(from)
                    .roomId(roomId)
                    .build(), REQ_MEMBERS_OF_GROUP);
            sendDeptJobAnalyticsEvent(keyword);
        });

        deptJobPresenter.onCreate();

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
                    if (deptJobDataView.getItemCount() > 10) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (dy > 0) {
                            activity.setTabLayoutVisible(false);
                        } else {
                            activity.setTabLayoutVisible(true);
                        }
                    }

                }
            });
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

    private void sendDeptJobAnalyticsEvent(String keyword) {
        boolean isUnDefined = JandiApplication.getContext().getString(R.string.jandi_undefined_member).contains(keyword);

        if (screen == AnalyticsValue.Screen.SelectTeamMemberSearch) {
            AnalyticsValue.Action action = type == EXTRA_TYPE_DEPT
                    ? AnalyticsValue.Action.ChooseDepartment
                    : AnalyticsValue.Action.ChooseJobTitle;
            AnalyticsUtil.sendEvent(screen, action);
            return;
        }

        if (type == EXTRA_TYPE_DEPT) {
            AnalyticsUtil.sendEvent(screen,
                    isUnDefined ? AnalyticsValue.Action.ChooseDepartment_Undefined
                            : AnalyticsValue.Action.ChooseDepartment);
        } else if (type == EXTRA_TYPE_JOB) {
            AnalyticsUtil.sendEvent(screen,
                    isUnDefined ? AnalyticsValue.Action.ChooseJobTitle_Undefined
                            : AnalyticsValue.Action.ChooseJobTitle);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQ_MEMBERS_OF_GROUP == requestCode && resultCode == Activity.RESULT_OK) {
            if (selectMode) {
                if (roomId < 0) {
                    // pick mode
                    long userId = data.getLongExtra(DeptJobGroupActivity.EXTRA_RESULT, -1);
                    if (userId > 0) {
                        deptJobPresenter.onPickUser(userId);
                    } else {
                        ColoredToast.show(R.string.err_profile_get_info);
                    }

                } else {
                    // multi select mode
                    long[] toggledIds = data.getLongArrayExtra(DeptJobGroupActivity.EXTRA_RESULT);
                    if (toggledIds != null
                            && getActivity() instanceof ToggledUserView) {
                        ((ToggledUserView) getActivity()).addToggledUser(toggledIds);
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        deptJobPresenter.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    public void onEvent(MemberStarredEvent event) {
        deptJobPresenter.onRefresh();
    }

    public void onEvent(ProfileChangeEvent event) {
        deptJobPresenter.onRefresh();
    }

    public void onEvent(MemberRankUpdatedEvent event) {
        deptJobPresenter.onRefresh();
    }

    @Override
    public void refreshDataView() {
        deptJobDataView.refresh();
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
        getActivity().finish();
    }

    @Override
    public void dismissEmptyView() {
        vgEmpty.setVisibility(View.GONE);
    }

    @Override
    public void showEmptyView(String keyword) {
        vgEmpty.setVisibility(View.VISIBLE);
        String textFormat = getString(R.string.jandi_has_no_searched_member_333333, keyword);
        tvEmpty.setText(Html.fromHtml(String.format(textFormat, keyword)));
    }

    @Override
    public void setKeywordObservable(Observable<String> keywordObservable) {
        keywordObservable.subscribe(text -> {
            if (deptJobPresenter != null) {
                deptJobPresenter.onSearchKeyword(text);
            }
        });
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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            setAnalyticsScreen();
        }
    }

    public void onEvent(TeamJoinEvent event) {
        deptJobPresenter.onRefresh();
    }

    public void onEvent(TeamLeaveEvent event) {
        deptJobPresenter.onRefresh();
    }

    public void onEvent(TeamInfoChangeEvent event) {
        deptJobPresenter.onRefresh();
    }

}
