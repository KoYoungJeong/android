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

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceStarter;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.profile.account.SettingAccountProfileActivity;
import com.tosslab.jandi.app.ui.team.create.CreateTeamForNewAccountActivity;
import com.tosslab.jandi.app.ui.team.select.adapter.TeamSelectListAdapter;
import com.tosslab.jandi.app.ui.team.select.adapter.viewmodel.TeamSelectListAdapterViewModel;
import com.tosslab.jandi.app.ui.team.select.dagger.DaggerTeamSelectListComponent;
import com.tosslab.jandi.app.ui.team.select.dagger.TeamSelectListModule;
import com.tosslab.jandi.app.ui.team.select.presenter.TeamSelectListPresenter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 2016. 9. 27..
 */

public class TeamSelectListActivity extends BaseAppCompatActivity implements TeamSelectListPresenter.View {

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_team_list);
        TeamSelectListAdapter teamSelectListAdapter = new TeamSelectListAdapter();
        DaggerTeamSelectListComponent.builder()
                .teamSelectListModule(
                        new TeamSelectListModule(this, teamSelectListAdapter))
                .build()
                .inject(this);
        ButterKnife.bind(this);
        lvTeamList.setLayoutManager(new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false));
        lvTeamList.setAdapter(teamSelectListAdapter);
        bindListeners();
        teamSelectListPresenter.initTeamDatas(true);
        progressWheel = new ProgressWheel(this);
        teamSelectListPresenter.setUserEmailInfo();

        setEditButton();
    }

    private void setEditButton() {
        tvLoginInfoEditButton.setPaintFlags(
                tvLoginInfoEditButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvLoginInfoEditButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingAccountProfileActivity.class);
            startActivity(intent);
        });
    }

    private void bindListeners() {
        adapterViewModel.setOnTeamCreateClickListener(() -> {
            moveCreateTeam();
        });

        adapterViewModel.setOnTeamItemClickListener(teamId -> {
            teamSelectListPresenter.onEnterSelectedTeam(teamId);
        });

        adapterViewModel.setOnClickTeamJoinAcceptListener(team -> {
            teamSelectListPresenter.onRequestAcceptJoin(team);
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
    public void moveCreateTeam() {
        Intent intent = new Intent(this, CreateTeamForNewAccountActivity.class);
        startActivity(intent);
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
    public void showErrorToast(String message) {
        ColoredToast.showError(message);
    }

    @Override
    public void showLoginEmail(String email) {
        tvLoginEmail.setText(email);
    }

}