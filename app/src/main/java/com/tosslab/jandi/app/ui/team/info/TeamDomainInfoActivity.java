package com.tosslab.jandi.app.ui.team.info;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
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

/**
 * Created by Steve SeongUg Jung on 14. 12. 17..
 */
@EActivity(R.layout.activity_team_domain_info)
@OptionsMenu(R.menu.teamdomain_info)
public class TeamDomainInfoActivity extends Activity {

    @Extra()
    String mode = "INFO";

    @Bean
    TeamDomainInfoModel teamDomainInfoModel;

    @Bean
    TeamDomainInfoPresenter teamDomainInfoPresenter;

    @AfterViews
    void initView() {

        Mode activityMode = Mode.valueOf(mode);

        switch (activityMode) {
            case CREATE:
                teamDomainInfoPresenter.setTeamCreatable(true);
                break;
            case INFO:
                teamDomainInfoPresenter.setTeamCreatable(false);
                break;
        }

        setUpActionBar(activityMode);

        teamDomainInfoModel.setCallback(new TeamDomainInfoModel.Callback() {
            @Override
            public void onTeamCreateSuccess(String name) {
                teamDomainInfoPresenter.successCreateTeam(name);
            }

            @Override
            public void onTeamCreateFail(int statusCode) {
                teamDomainInfoPresenter.failCreateTeam(statusCode);

            }
        });


        initUserEmailInfo();


    }

    @Background
    void initUserEmailInfo() {
        List<ResAccountInfo.UserEmail> userEmails = teamDomainInfoModel.initUserEmailInfo();
        if (userEmails != null && userEmails.size() > 0) {
            teamDomainInfoPresenter.setEmails(userEmails);
        } else {
            ColoredToast.showWarning(TeamDomainInfoActivity.this, getString(R.string.err_network));
            finish();
        }

    }

    @OptionsItem(android.R.id.home)
    void goHomeUpMenu() {
        finish();
    }


    @OptionsItem(R.id.action_confirm)
    void confirmTeamDomain() {
        Mode activityMode = Mode.valueOf(mode);
        if (activityMode == Mode.INFO) {
            finish();
            return;
        }

        // Team Creation
        String teamName = teamDomainInfoPresenter.getTeamName();
        String teamDomain = teamDomainInfoPresenter.getTeamDomain();
        String myName = teamDomainInfoPresenter.getMyName();
        String myEmail = teamDomainInfoPresenter.getMyEmail();
        teamDomainInfoModel.createNewTeam(teamName, teamDomain, myName, myEmail);

    }

    private void setUpActionBar(Mode mode) {
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        switch (mode) {
            case CREATE:
                actionBar.setTitle(R.string.team_create);
                break;
            case INFO:
                actionBar.setTitle(R.string.team_info);
                break;
        }
    }


    public enum Mode {
        CREATE, INFO
    }

}
