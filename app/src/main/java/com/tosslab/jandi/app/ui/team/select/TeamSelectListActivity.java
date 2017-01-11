package com.tosslab.jandi.app.ui.team.select;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.team.TeamDeletedEvent;
import com.tosslab.jandi.app.events.team.TeamJoinEvent;
import com.tosslab.jandi.app.events.team.TeamLeaveEvent;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceStarter;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.profile.account.SettingAccountProfileActivity;
import com.tosslab.jandi.app.ui.team.select.adapter.TeamSelectListAdapter;
import com.tosslab.jandi.app.ui.team.select.adapter.viewmodel.TeamSelectListAdapterViewModel;
import com.tosslab.jandi.app.ui.team.select.dagger.DaggerTeamSelectListComponent;
import com.tosslab.jandi.app.ui.team.select.dagger.TeamSelectListModule;
import com.tosslab.jandi.app.ui.team.select.presenter.TeamSelectListPresenter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by tee on 2016. 9. 27..
 */

public class TeamSelectListActivity extends BaseAppCompatActivity implements TeamSelectListPresenter.View {

    public static final int REQ_ACCOUNT_SETTING = 301;
    @Inject
    TeamSelectListPresenter teamSelectListPresenter;

    @Inject
    TeamSelectListAdapterViewModel adapterViewModel;

    @Bind(R.id.tv_login_info_edit_button)
    TextView tvLoginInfoEditButton;

    @Bind(R.id.tv_login_email)
    TextView tvLoginEmail;

    @Bind(R.id.vg_no_joined_team)
    ViewGroup vgNoJoinedTeam;

    @Bind(R.id.tv_joined_team_title)
    TextView tvJoinedTeamTitle;

    @Bind(R.id.lv_team_list)
    RecyclerView lvTeamList;

    ProgressWheel progressWheel;

    @Nullable
    @InjectExtra
    boolean shouldRefreshAccountInfo = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_team_list);
        ButterKnife.bind(this);
        Dart.inject(this);
        TeamSelectListAdapter teamSelectListAdapter = new TeamSelectListAdapter();
        DaggerTeamSelectListComponent.builder()
                .teamSelectListModule(
                        new TeamSelectListModule(this, teamSelectListAdapter))
                .build()
                .inject(this);
        lvTeamList.setLayoutManager(new LinearLayoutManager(this));
        lvTeamList.setAdapter(teamSelectListAdapter);
        bindListeners();
        teamSelectListPresenter.initTeamDatas(true, shouldRefreshAccountInfo);
        teamSelectListPresenter.setUserEmailInfo();
        progressWheel = new ProgressWheel(this);
        setEditButton();
        EventBus.getDefault().register(this);
        JandiSocketService.startServiceIfNeed(this);
    }

    private void setEditButton() {
        tvLoginInfoEditButton.setPaintFlags(
                tvLoginInfoEditButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvLoginInfoEditButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingAccountProfileActivity.class);
            startActivityForResult(intent, REQ_ACCOUNT_SETTING);
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.AccountHome, AnalyticsValue.Action.EditAccount);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_ACCOUNT_SETTING) {
            teamSelectListPresenter.setUserEmailInfo();
        }
    }

    private void bindListeners() {
        adapterViewModel.setOnTeamCreateClickListener(() -> {
            moveCreateTeam(false);
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.AccountHome, AnalyticsValue.Action.CreateTeam);
        });

        adapterViewModel.setOnTeamItemClickListener(teamId -> {
            teamSelectListPresenter.onEnterSelectedTeam(teamId);
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.AccountHome, AnalyticsValue.Action.ChooseTeam);
        });

        adapterViewModel.setOnClickTeamJoinAcceptListener(team -> {
            teamSelectListPresenter.onRequestAcceptJoin(team);
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.AccountHome, AnalyticsValue.Action.AcceptTeamInvitation);
        });

        adapterViewModel.setOnClickTeamJoinIgnoreListener(team -> {
            teamSelectListPresenter.onRequestIgnoreJoin(team, true);
        });

    }

    @Override
    public void justRefresh() {
        adapterViewModel.justRefresh();
    }

    @Override
    public void showEmptyList() {
        vgNoJoinedTeam.setVisibility(View.VISIBLE);
        tvJoinedTeamTitle.setVisibility(View.GONE);
        justRefresh();
    }

    @Override
    public void showList() {
        vgNoJoinedTeam.setVisibility(View.GONE);
        tvJoinedTeamTitle.setVisibility(View.VISIBLE);
        justRefresh();
    }

    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void showProgressWheel() {
        dismissProgressWheel();

        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void moveSelectedTeam() {
        JandiSocketService.stopService(this);
        sendBroadcast(new Intent(SocketServiceStarter.START_SOCKET_SERVICE));

        startActivity(Henson.with(this)
                .gotoMainTabActivity()
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP));
        overridePendingTransition(0, 0);

        finish();
    }

    @Override
    public void moveCreateTeam(boolean isFirstEntered) {
        startActivity(Henson.with(this)
                .gotoCreateTeamForNewAccountActivity()
                .isFirstExecution(isFirstEntered)
                .build());
    }

    @Override
    public void showTextAlertDialog(String msg, DialogInterface.OnClickListener clickListener) {
        new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.jandi_confirm),
                        clickListener)
                .create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void showErrorToast(String message) {
        ColoredToast.showError(message);
    }

    @Override
    public void showLoginEmail(String email) {
        tvLoginEmail.setText(email);
    }

    public void onEvent(TeamDeletedEvent event) {
        teamSelectListPresenter.initTeamDatas(false, true);
    }

    public void onEvent(TeamLeaveEvent event) {
        teamSelectListPresenter.initTeamDatas(false, true);
    }

    public void onEvent(TeamJoinEvent event) {
        teamSelectListPresenter.initTeamDatas(false, true);
    }

}