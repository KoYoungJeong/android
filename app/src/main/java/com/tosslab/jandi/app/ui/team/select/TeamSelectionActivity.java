package com.tosslab.jandi.app.ui.team.select;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.team.invite.TeamInviteAcceptEvent;
import com.tosslab.jandi.app.events.team.invite.TeamInviteIgnoreEvent;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
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
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 14. 11. 14..
 */
@EActivity(R.layout.activity_team_selection)
public class TeamSelectionActivity extends ActionBarActivity {

    public static final int CALLED_MUST_SELECT_TEAM = 100;
    @Extra
    int calledType = CALLED_MUST_SELECT_TEAM;
    public static final int CALLED_CHANGE_TEAM = 101;
    public static final int REQ_TEAM_CREATE = 2031;
    private static final int REQ_TEAM_JOIN = 2032;
    private static final Logger logger = Logger.getLogger(TeamSelectionActivity.class);
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

    @ItemClick(R.id.lv_intro_team_list)
    void teamItemClick(int position) {

        Team selectedMyTeam = teamSelectionPresenter.getItem(position);

        Team.Status status = selectedMyTeam.getStatus();

        switch (status) {
            case JOINED:
                selectedMyTeam.setSelected(true);
                int lastSelectedPosition = teamSelectionPresenter.getLastSelectedPosition();
                if (lastSelectedPosition != -1) {
                    teamSelectionPresenter.getItem(lastSelectedPosition).setSelected(false);
                }
                teamSelectionPresenter.notifyDataSetChanged();
                int mSelectedTeamId = selectedMyTeam.getTeamId();
                lastSelectedPosition = position != teamSelectionPresenter.getLastSelectedPosition() ? position : -1;
                teamSelectionPresenter.setLastSelectedPosition(lastSelectedPosition);
                logger.debug(selectedMyTeam.getName() + ", id=" + mSelectedTeamId + ", is selected : " + selectedMyTeam.isSelected());

                selectTeam();

                break;
            case PENDING:
                // nothing action
                break;
            case CREATE:
                // create team action
                TeamDomainInfoActivity_.intent(TeamSelectionActivity.this)
                        .mode(TeamDomainInfoActivity.Mode.CREATE.name())
                        .startForResult(TeamSelectionActivity.REQ_TEAM_CREATE);
                break;
        }
    }

    @Background
    void selectTeam() {

        teamSelectionPresenter.showProgressWheel();
        Team lastSelectedItem = teamSelectionPresenter.getLastSelectedItem();
        teamSelectionModel.updateSelectedTeam(lastSelectedItem);
        try {
            ResLeftSideMenu resLeftSideMenu = teamSelectionModel.updateIdentityManager(lastSelectedItem.getTeamId());
            teamSelectionModel.setEntityManager(resLeftSideMenu);

        } catch (JandiNetworkException e) {
            e.printStackTrace();
        } finally {
            teamSelectionPresenter.dismissProgressWheel();
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
        } finally {
            teamSelectionPresenter.dismissProgressWheel();
        }

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        // Set up the action bar.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

}
