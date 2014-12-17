package com.tosslab.jandi.app.ui.team.select;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.ResultObject;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.ui.team.select.model.TeamSelectionModel;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.rest.RestService;

import java.util.ArrayList;

/**
 * Created by justinygchoi on 14. 11. 14..
 */
@EActivity(R.layout.activity_team_selection)
public class TeamSelectionActivity extends Activity {

    @RestService
    JandiRestClient mJandiRestClient;

    @Bean
    TeamSelectionPresenter teamSelectionPresenter;

    @Bean
    TeamSelectionModel teamSelectionModel;

    @AfterViews
    void initView() {
        setUpActionBar();

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

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
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
