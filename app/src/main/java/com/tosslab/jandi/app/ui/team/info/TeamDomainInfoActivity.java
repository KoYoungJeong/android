package com.tosslab.jandi.app.ui.team.info;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ReqInvitationConfirmOrIgnore;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 14. 12. 17..
 */
@EActivity(R.layout.activity_team_domain_info)
@OptionsMenu(R.menu.teamdomain_info)
public class TeamDomainInfoActivity extends ActionBarActivity {

    public static final int NOT_AVAILABLE_INVITATION_CODE = 40003;
    public static final int DISABLED_MEMBER = 40301;
    public static final int REMOVED_TEAM = 40302;
    public static final int TEAM_INVITATION_DISABLED = 40303;
    public static final int ENABLED_MEMBER = 40304;
    @Extra
    String mode = "CREATE";

    @Extra
    String token;
    @Extra
    String domain;
    @Extra
    String teamName;
    @Extra
    int teamId;
    @Extra
    String invitationId;
    @Bean
    TeamDomainInfoModel teamDomainInfoModel;
    @Bean
    TeamDomainInfoPresenter teamDomainInfoPresenter;
    private String userEmail;
    private String userName;

    @AfterViews
    void initView() {

        Mode activityMode = Mode.valueOf(mode);


        switch (activityMode) {
            case CREATE:
                MixpanelMemberAnalyticsClient.getInstance(TeamDomainInfoActivity.this, null)
                        .pageViewTeamCreate();
                teamDomainInfoPresenter.setTeamCreatable(true);
                break;
            case JOIN:
                MixpanelMemberAnalyticsClient.getInstance(TeamDomainInfoActivity.this, null)
                        .pageViewMemberCreate();
                teamDomainInfoPresenter.setTeamDomain(domain);
                teamDomainInfoPresenter.setTeamName(teamName);
                teamDomainInfoPresenter.setTeamCreatable(false);
                break;
        }

        setUpActionBar(activityMode);

        teamDomainInfoModel.setCallback(new TeamDomainInfoModel.Callback() {
            @Override
            public void onTeamCreateSuccess(String name, int memberId, int teamId) {


            }

            @Override
            public void onTeamCreateFail(int statusCode) {
                teamDomainInfoPresenter.dismissProgressWheel();
                teamDomainInfoPresenter.failCreateTeam(statusCode);

            }
        });

        initUserEmailInfo();
        initUserDefaultName();

    }

    @Background
    void initUserDefaultName() {
        userName = teamDomainInfoModel.getUserName();
    }

    @Background
    void initUserEmailInfo() {
        List<ResAccountInfo.UserEmail> userEmails = teamDomainInfoModel.initUserEmailInfo();
        if (userEmails != null && userEmails.size() > 0) {
            userEmail = userEmails.get(0).getId();
        } else {
            ColoredToast.showWarning(TeamDomainInfoActivity.this, getString(R.string.err_network));
            finish();
        }
    }

    @OptionsItem(android.R.id.home)
    void goHomeUpMenu() {
        finish();
    }


    @Background
    @OptionsItem(R.id.action_confirm)
    void confirmTeamDomain() {
        Mode activityMode = Mode.valueOf(mode);
        if (activityMode == Mode.JOIN) {

            joinTeam(invitationId);
        } else {

            String teamName = teamDomainInfoPresenter.getTeamName();
            String teamDomain = teamDomainInfoPresenter.getTeamDomain();
            String myName = userName;
            String myEmail = userEmail;

            if (TextUtils.isEmpty(teamDomain)) {
                teamDomainInfoPresenter.showFailToast(getString(R.string.err_invalid_team_domain));
                return;
            }

            createTeam(teamName, teamDomain.toLowerCase(), myName, myEmail);
        }


    }

    private void createTeam(String teamName, String teamDomain, String myName, String myEmail) {
        teamDomainInfoPresenter.showProgressWheel();

        // Team Creation
        try {
            ResTeamDetailInfo newTeam = teamDomainInfoModel.createNewTeam(teamName, teamDomain, myName, myEmail);

            String distictId = newTeam.getInviteTeam().getId() + "-" + newTeam.getInviteTeam().getTeamId();
            MixpanelMemberAnalyticsClient.getInstance(TeamDomainInfoActivity.this, null)
                    .pageViewTeamCreateSuccess();

            teamDomainInfoModel.updateTeamInfo(newTeam.getInviteTeam().getTeamId());
            teamDomainInfoPresenter.dismissProgressWheel();
            teamDomainInfoPresenter.successCreateTeam(newTeam.getInviteTeam().getName());

        } catch (JandiNetworkException e) {
            teamDomainInfoPresenter.dismissProgressWheel();
            teamDomainInfoPresenter.failCreateTeam(e.httpStatusCode);
        }
    }

    void joinTeam(String invitationId) {
        teamDomainInfoPresenter.showProgressWheel();

        try {
            teamDomainInfoModel.acceptOrDclineInvite(invitationId, ReqInvitationConfirmOrIgnore.Type.ACCEPT.getType());
            teamDomainInfoModel.updateTeamInfo(teamId);
            teamDomainInfoPresenter.successJoinTeam();
            MixpanelMemberAnalyticsClient.getInstance(TeamDomainInfoActivity.this, null)
                    .pageViewMemberCreateSuccess();
        } catch (JandiNetworkException e) {
            e.printStackTrace();

            if (e.errCode == NOT_AVAILABLE_INVITATION_CODE) {
                alertTextDialog(getResources().getString(R.string.jandi_expired_invitation_link));
            } else if (e.errCode == DISABLED_MEMBER) {
                alertTextDialog(getResources().getString(R.string.jandi_disabled_team, teamName));
            } else if (e.errCode == REMOVED_TEAM) {
                alertTextDialog(getResources().getString(R.string.jandi_deleted_team));
            } else if (e.errCode == TEAM_INVITATION_DISABLED) {
                alertTextDialog(getResources().getString(R.string.jandi_invite_disabled));
            } else if (e.errCode == ENABLED_MEMBER) {
                alertTextDialog(getResources().getString(R.string.jandi_joined_team, teamName));
            }
            teamDomainInfoPresenter.failJoinTeam();
        }

        teamDomainInfoPresenter.dismissProgressWheel();

    }

    @UiThread
    public void alertTextDialog(String alertText) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder
                .setMessage(alertText)
                .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.jandi_confirm),
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                // 다이얼로그를 취소한다
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    private void setUpActionBar(Mode mode) {

        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        // Set up the action bar.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

    }

    public enum Mode {
        CREATE, JOIN
    }

}
