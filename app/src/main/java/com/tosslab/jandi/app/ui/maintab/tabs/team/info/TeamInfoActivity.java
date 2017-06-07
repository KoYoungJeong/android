package com.tosslab.jandi.app.ui.maintab.tabs.team.info;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.PollRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.client.account.teams.AccountTeamsApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.views.profile.ProfileLabelView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TeamInfoActivity extends BaseAppCompatActivity {

    @Bind(R.id.tv_team_info_app_version)
    TextView tvAppVersion;

    @Bind(R.id.actionbar_team_info)
    Toolbar toolbar;

    @Bind(R.id.label_team_info_name)
    ProfileLabelView labelTeamName;

    @Bind(R.id.label_team_info_url)
    ProfileLabelView labelTeamUrl;

    @Bind(R.id.label_team_info_admin)
    ProfileLabelView labelTeamAdmin;

    @Bind(R.id.label_team_info_member_count)
    ProfileLabelView labelTeamMemberCount;

    @Bind(R.id.tv_team_leave)
    TextView tvTeamLeave;

    public static void start(Context context) {
        Intent starter = new Intent(context, TeamInfoActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_info);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        setUpTeamName();
        setUpTeamUrl();
        setUpTeamAdmin();
        setUpMemberCount();
        setUpAppVersion();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.tv_team_leave)
    void leaveTeam() {
        // team leave 로직
        if (TeamInfoLoader.getInstance().getMyLevel() == Level.Owner) {
            showNoAllowedTeamLeaveDialog();
        } else {
            showTeamLeaveDialog();
        }
    }

    private void showTeamLeaveDialog() {
        new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setTitle(R.string.team_teamleave_member_title)
                .setMessage(R.string.team_teamleave_member_desc)
                .setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
                    Completable.fromCallable(() -> {
                        new AccountTeamsApi(InnerApiRetrofitBuilder.getInstance()).requestLeaveTeam(TeamInfoLoader.getInstance().getTeamId());
                        return Completable.complete();
                    }).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                                leaveTeamAndGotoSelectTeam();
                            }, t -> {
                                t.printStackTrace();
                            });
                })
                .setNegativeButton(R.string.jandi_cancel, null)
                .create()
                .show();
    }

    private void leaveTeamAndGotoSelectTeam() {
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        AccountRepository.getRepository().removeSelectedTeamInfo();
        startActivity(Henson.with(TeamInfoActivity.this)
                .gotoTeamSelectListActivity()
                .shouldRefreshAccountInfo(true)
                .build());

        InitialInfoRepository.getInstance().removeInitialInfo(teamId);
        JandiPreference.setSocketConnectedLastTime(-1);

        PollRepository.getInstance().clear(teamId);

        TeamInfoLoader instance = TeamInfoLoader.getInstance();
        instance = null;
    }

    private void showNoAllowedTeamLeaveDialog() {
        new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setTitle(R.string.team_teamleave_owner_title)
                .setMessage(R.string.team_teamleave_owner_desc)
                .setPositiveButton(R.string.jandi_confirm, null)
                .create()
                .show();
    }

    private void setUpAppVersion() {
        tvAppVersion.setText(String.format("%s : %s", "App Version", ApplicationUtil.getAppVersionName()));
    }

    private void setUpMemberCount() {
        // JandiBot 을 제거해줘야 함.
        Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(User::isEnabled)
                .count()
                .defaultIfEmpty(0)
                .subscribe(it -> labelTeamMemberCount.setTextContent(String.valueOf(it - 1)));
    }

    private void setUpTeamAdmin() {
        Observable.from(TeamInfoLoader.getInstance().getUserList())
                .takeFirst((user) -> user.getLevel() == Level.Owner)
                .map(User::getName)
                .defaultIfEmpty("")
                .subscribe(it -> labelTeamAdmin.setTextContent(it));
    }

    private void setUpTeamUrl() {
        String teamDomain = TeamInfoLoader.getInstance().getTeamDomain();
        String url = String.format("%s.jandi.com", teamDomain);
        labelTeamUrl.setTextContent(url);
    }

    private void setUpTeamName() {
        String teamName = TeamInfoLoader.getInstance().getTeamName();
        labelTeamName.setTextContent(teamName);
    }
}
