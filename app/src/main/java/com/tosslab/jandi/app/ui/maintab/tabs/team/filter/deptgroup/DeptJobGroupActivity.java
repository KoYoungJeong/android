package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.deptgroup;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.DeptJobFragment;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.deptgroup.dagger.DaggerDeptJobGroupComponent;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.deptgroup.dagger.DeptJobGroupModule;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.deptgroup.presenter.DeptJobGroupPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.TeamMemberAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.TeamMemberDataView;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class DeptJobGroupActivity extends BaseAppCompatActivity implements DeptJobGroupPresenter.View {

    public static final String EXTRA_RESULT = "result";
    @Nullable
    @InjectExtra
    int type;

    @Nullable
    @InjectExtra
    String keyword;

    @Nullable
    @InjectExtra
    boolean selectMode;

    @Nullable
    @InjectExtra
    boolean pickMode = false;

    @Bind(R.id.list_dept_job_group)
    RecyclerView lvMembers;

    @Inject
    DeptJobGroupPresenter presenter;

    @Inject
    TeamMemberDataView teamMemberDataView;

    @Bind(R.id.layout_dept_job_group_bar)
    Toolbar toolbar;

    @Bind(R.id.vg_dept_job_group_toggled)
    android.view.View vgToggled;

    @Bind(R.id.tv_dept_job_group_toggled_invite)
    TextView tvAdded;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dept_job_group);

        ButterKnife.bind(this);
        Dart.inject(this);

        initActionbar();

        TeamMemberAdapter teamMemberAdapter = new TeamMemberAdapter();
        teamMemberAdapter.setSelectedMode(selectMode && !pickMode);
        lvMembers.setLayoutManager(new LinearLayoutManager(DeptJobGroupActivity.this));
        lvMembers.setAdapter(teamMemberAdapter);

        DaggerDeptJobGroupComponent.builder()
                .deptJobGroupModule(new DeptJobGroupModule(this, teamMemberAdapter,
                        type,
                        keyword,
                        selectMode, pickMode))
                .build()
                .inject(this);

        teamMemberAdapter.setOnItemClickListener((view, adapter, position) -> {
            presenter.onMemberClick(position);
        });

        presenter.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    public void onEvent(MemberStarredEvent event) {
        presenter.onRefresh();
    }

    public void onEvent(ProfileChangeEvent event) {
        presenter.onRefresh();
    }

    private void initActionbar() {

        toolbar.setTitle(keyword);
        setSupportActionBar(toolbar);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void refreshDataView() {
        teamMemberDataView.refresh();
    }

    @Override
    public void pickUser(long userId) {
        if (pickMode) {
            AnalyticsValue.Action action = type == DeptJobFragment.EXTRA_TYPE_DEPT
                    ? AnalyticsValue.Action.ChooseDepartment_Undefined
                    : AnalyticsValue.Action.ChooseJobTitle_Undefined;
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SelectTeamMember, action);

            Intent intent = new Intent();
            intent.putExtra(EXTRA_RESULT, userId);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            MemberProfileActivity_.intent(this)
                    .from(MemberProfileActivity.EXTRA_FROM_TEAM_MEMBER)
                    .memberId(userId)
                    .start();
        }
    }

    @Override
    public void updateToggledUser(int count) {
        if (count <= 0) {
            vgToggled.setVisibility(View.GONE);
        } else {
            vgToggled.setVisibility(View.VISIBLE);
        }

        tvAdded.setText(getString(R.string.jandi_invite_member_count, count));
    }

    @Override
    public void comeWithResult(long[] toggledUser) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT, toggledUser);
        setResult(RESULT_OK, intent);
        finish();
    }

    @OnClick(R.id.tv_dept_job_group_toggled_unselect_all)
    void onUnselectClick() {
        presenter.onUnselectClick();

        AnalyticsValue.Screen screen = type == DeptJobFragment.EXTRA_TYPE_DEPT
                ? AnalyticsValue.Screen.InviteTeamMembers_Department
                : AnalyticsValue.Screen.InviteTeamMembers_JobTitle;

        AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.CancelSelect);
    }

    @OnClick(R.id.tv_dept_job_group_toggled_invite)
    void onAddClick() {
        presenter.onAddClick();

        AnalyticsValue.Screen screen = type == DeptJobFragment.EXTRA_TYPE_DEPT
                ? AnalyticsValue.Screen.InviteTeamMembers_Department
                : AnalyticsValue.Screen.InviteTeamMembers_JobTitle;

        AnalyticsUtil.sendEvent(screen, AnalyticsValue.Action.InviteMembers);
    }
}
