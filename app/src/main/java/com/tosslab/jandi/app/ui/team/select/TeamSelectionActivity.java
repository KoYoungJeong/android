package com.tosslab.jandi.app.ui.team.select;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.Menu;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.team.invite.TeamInviteAcceptEvent;
import com.tosslab.jandi.app.events.team.invite.TeamInviteIgnoreEvent;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.ResultObject;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.ui.team.info.TeamDomainInfoActivity;
import com.tosslab.jandi.app.ui.team.info.TeamDomainInfoActivity_;
import com.tosslab.jandi.app.ui.team.select.model.TeamSelectionModel;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 14. 11. 14..
 */
@EActivity(R.layout.activity_team_selection)
public class TeamSelectionActivity extends Activity {

    public final static int CALLED_MUST_SELECT_TEAM = 100;
    @Extra
    int calledType = CALLED_MUST_SELECT_TEAM;
    public static final int CALLED_CHANGE_TEAM = 101;
    public static final int REQ_TEAM_CREATE = 2031;
    private static final int REQ_TEAM_JOIN = 2032;
    @Bean
    TeamSelectionPresenter teamSelectionPresenter;

    @Bean
    TeamSelectionModel teamSelectionModel;

    @AfterViews
    void initView() {
        setUpActionBar();

        teamSelectionPresenter.showProgressWheel();

        MixpanelMemberAnalyticsClient.getInstance(TeamSelectionActivity.this, null)
                .flush()
                .clear();

        getTeamList();
    }

    @Background
    void getTeamList() {


        ResultObject<ArrayList<Team>> resultObject = teamSelectionModel.getMyTeamList();


        if (resultObject.getStatusCode() < 400) {
            // Success
            teamSelectionPresenter.setTeamList(resultObject.getResult());
        } else {
            teamSelectionPresenter.showErrorToast(resultObject.getStatusCode(), resultObject.getErrorMsg());
        }

        teamSelectionPresenter.dismissProgressWheel();

    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    public void onEvent(TeamInviteAcceptEvent teamInviteAcceptEvent) {

        Team team = teamInviteAcceptEvent.getTeam();
        TeamDomainInfoActivity_.intent(TeamSelectionActivity.this)
                .mode(TeamDomainInfoActivity.Mode.JOIN.name())
                .teamId(team.getTeamId())
                .teamName(team.getName())
                .domain(team.getTeamDomain())
                .token(team.getToken())
                .startForResult(REQ_TEAM_JOIN);

    }

    public void onEvent(TeamInviteIgnoreEvent teamInviteIgnoreEvent) {
        ignoreInviteTeam(teamInviteIgnoreEvent);

    }

    @Background
    void ignoreInviteTeam(TeamInviteIgnoreEvent teamInviteIgnoreEvent) {
        teamSelectionPresenter.showProgressWheel();
        List<ResPendingTeamInfo> resPendingTeamInfos = teamSelectionModel.ignoreInvite(teamInviteIgnoreEvent.getTeam());

        if (resPendingTeamInfos != null) {
            teamSelectionModel.updateToDBJoinedTeamInfo();
            getTeamList();
        } else {
            teamSelectionPresenter.showErrorToast(400, "");

        }
        teamSelectionPresenter.dismissProgressWheel();
    }

    @Override
    public void onBackPressed() {
        movePreviousActivity();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.clear();

        if (teamSelectionPresenter.getLastSelectedPosition() != -1) {
            getMenuInflater().inflate(R.menu.team_select, menu);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @OptionsItem(R.id.action_confirm)
    void onSelectTeam() {

        selectTeam();

    }

    @Background
    void selectTeam() {

        Team lastSelectedItem = teamSelectionPresenter.getLastSelectedItem();
        teamSelectionModel.updateSelectedTeam(lastSelectedItem);
        teamSelectionModel.clearEntityManager();
        try {
            ResLeftSideMenu resLeftSideMenu = teamSelectionModel.updateIdentityManager(lastSelectedItem.getTeamId());
            teamSelectionModel.setEntityManager(resLeftSideMenu);

        } catch (JandiNetworkException e) {
            e.printStackTrace();
        }

        teamSelectionPresenter.selectTeam();
    }

    @OptionsItem(android.R.id.home)
    void goHome() {
        movePreviousActivity();

    }

    private void movePreviousActivity() {
        if (calledType == CALLED_MUST_SELECT_TEAM) {
            teamSelectionPresenter.showLogoutDialog();
        } else if (calledType == CALLED_CHANGE_TEAM) {

            ResAccountInfo.UserTeam selectedTeamInfo = JandiAccountDatabaseManager.getInstance(TeamSelectionActivity.this).getSelectedTeamInfo();
            String distictId = selectedTeamInfo.getMemberId() + "_" + selectedTeamInfo.getTeamId();
            MixpanelMemberAnalyticsClient.getInstance(TeamSelectionActivity.this, distictId)
                    .setNewIdentify(distictId);

            finish();
        }
    }

    @OnActivityResult(REQ_TEAM_CREATE)
    void onTeamCreateResult(int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {
            return;
        }

        teamDomainResult(true);

    }

    @Background
    void teamDomainResult(boolean isOwner) {
        teamSelectionPresenter.showProgressWheel();
        teamSelectionModel.clearEntityManager();

        JandiAccountDatabaseManager databaseManager = JandiAccountDatabaseManager.getInstance(TeamSelectionActivity.this);
        ResAccountInfo.UserTeam newSelectTeam = databaseManager.getSelectedTeamInfo();

        try {
            ResLeftSideMenu resLeftSideMenu = teamSelectionModel.updateIdentityManager(newSelectTeam.getTeamId());
            EntityManager entityManager = teamSelectionModel.setEntityManager(resLeftSideMenu);

            MixpanelMemberAnalyticsClient mixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(TeamSelectionActivity.this, null);
            mixpanelMemberAnalyticsClient.setNewIdentify(entityManager.getDistictId());
            mixpanelMemberAnalyticsClient.setPeoplekProfile(isOwner, resLeftSideMenu);

            mixpanelMemberAnalyticsClient.trackSignUp();
        } catch (JandiNetworkException e) {
            e.printStackTrace();
        }

        teamSelectionPresenter.dismissProgressWheel();
        teamSelectionPresenter.selectTeam();
    }

    @OnActivityResult(REQ_TEAM_JOIN)
    void onTeamJoinResult(int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {
            return;
        }

        teamDomainResult(false);
    }

    private void setUpActionBar() {
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

}
