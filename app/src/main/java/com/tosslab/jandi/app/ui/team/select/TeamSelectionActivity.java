package com.tosslab.jandi.app.ui.team.select;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.Menu;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.team.invite.TeamInviteAcceptEvent;
import com.tosslab.jandi.app.events.team.invite.TeamInviteIgnoreEvent;
import com.tosslab.jandi.app.network.ResultObject;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.ui.team.select.model.TeamSelectionModel;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.rest.RestService;

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
    public final static int CALLED_CHANGE_TEAM = 101;
    public static final int REQ_TEAM_CREATE = 2031;
    @RestService
    JandiRestClient mJandiRestClient;
    @Bean
    TeamSelectionPresenter teamSelectionPresenter;
    @Bean
    TeamSelectionModel teamSelectionModel;

    @AfterViews
    void initView() {
        setUpActionBar();

        teamSelectionPresenter.showProgressWheel();

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

        acceptInviteTeam(teamInviteAcceptEvent);

    }

    public void onEvent(TeamInviteIgnoreEvent teamInviteIgnoreEvent) {
        ignoreInviteTeam(teamInviteIgnoreEvent);

    }

    @Background
    void acceptInviteTeam(TeamInviteAcceptEvent teamInviteAcceptEvent) {
        teamSelectionPresenter.showProgressWheel();
        List<ResTeamDetailInfo> resTeamDetailInfos = teamSelectionModel.acceptInvite(teamInviteAcceptEvent.getTeam());
        if (resTeamDetailInfos != null) {
            teamSelectionModel.updateToDBJoinedTeamInfo();
            getTeamList();
        } else {
            teamSelectionPresenter.showErrorToast(400, "");
        }
        teamSelectionPresenter.dismissProgressWheel();
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
    void selectTeam() {

        Team lastSelectedItem = teamSelectionPresenter.getLastSelectedItem();
        teamSelectionModel.updateSelectedTeam(lastSelectedItem);
        teamSelectionPresenter.selectTeam(lastSelectedItem);
    }

    @OptionsItem(android.R.id.home)
    void goHome() {
        movePreviousActivity();

    }

    private void movePreviousActivity() {
        if (calledType == CALLED_MUST_SELECT_TEAM) {
            teamSelectionPresenter.showLogoutDialog();
        } else if (calledType == CALLED_CHANGE_TEAM) {
            finish();
        }
    }

    @OnActivityResult(REQ_TEAM_CREATE)
    void onTeamCreateResult(int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {
            return;
        }
        getTeamList();
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
