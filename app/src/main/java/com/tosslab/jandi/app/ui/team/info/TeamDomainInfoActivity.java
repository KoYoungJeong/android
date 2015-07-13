package com.tosslab.jandi.app.ui.team.info;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.exception.ConnectionNotFoundException;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 14. 12. 17..
 */
@EActivity(R.layout.activity_team_domain_info)
@OptionsMenu(R.menu.teamdomain_info)
public class TeamDomainInfoActivity extends AppCompatActivity {

    @Extra
    String token;
    @Extra
    String domain;
    @Extra
    String teamName;
    @Extra
    int teamId;
    @Bean
    TeamDomainInfoModel teamDomainInfoModel;
    @Bean
    TeamDomainInfoPresenter teamDomainInfoPresenter;
    private String userEmail;
    private String userName;

    @AfterViews
    void initView() {
        MixpanelMemberAnalyticsClient.getInstance(TeamDomainInfoActivity.this, null)
                .pageViewTeamCreate();
        teamDomainInfoPresenter.setTeamCreatable(true);

        setUpActionBar();

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

    private void createTeam(String teamName, String teamDomain, String myName, String myEmail) {
        teamDomainInfoPresenter.showProgressWheel();

        // Team Creation
        try {
            ResTeamDetailInfo newTeam = teamDomainInfoModel.createNewTeam(teamName, teamDomain);

            String distictId = newTeam.getInviteTeam().getId() + "-" + newTeam.getInviteTeam().getTeamId();
            MixpanelMemberAnalyticsClient.getInstance(TeamDomainInfoActivity.this, null)
                    .pageViewTeamCreateSuccess();

            teamDomainInfoModel.updateTeamInfo(newTeam.getInviteTeam().getTeamId());
            teamDomainInfoPresenter.dismissProgressWheel();
            teamDomainInfoPresenter.successCreateTeam(newTeam.getInviteTeam().getName());

        } catch (RetrofitError e) {
            e.printStackTrace();
            teamDomainInfoPresenter.dismissProgressWheel();
            if (e.getCause() instanceof ConnectionNotFoundException) {
                teamDomainInfoPresenter.showFailToast(getString(R.string.err_network));
                return;
            }
            teamDomainInfoPresenter.failCreateTeam(e.getResponse().getStatus());
        }
    }

    private void setUpActionBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        // Set up the action bar.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

    }
}
