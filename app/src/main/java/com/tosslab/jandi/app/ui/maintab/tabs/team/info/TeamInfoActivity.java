package com.tosslab.jandi.app.ui.maintab.tabs.team.info;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.invites.InviteDialogExecutor;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.profile.ProfileLabelView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;

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

    @OnClick(R.id.btn_team_info_invite)
    void inviteMember() {
        InviteDialogExecutor.getInstance().executeInvite(this);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamInformation, AnalyticsValue.Action.InviteMember);
    }

    private void setUpAppVersion() {
        tvAppVersion.setText(String.format("%s : %s", "App Version", ApplicationUtil.getAppVersionName()));
    }

    private void setUpMemberCount() {
        Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(User::isEnabled)
                .count()
                .defaultIfEmpty(0)
                .subscribe(it -> labelTeamMemberCount.setTextContent(String.valueOf(it)));

    }

    private void setUpTeamAdmin() {
        Observable.from(TeamInfoLoader.getInstance().getUserList())
                .takeFirst(User::isTeamOwner)
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
