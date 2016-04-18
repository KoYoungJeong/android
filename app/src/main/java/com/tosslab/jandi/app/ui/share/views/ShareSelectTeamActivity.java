package com.tosslab.jandi.app.ui.share.views;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.share.ShareSelectTeamEvent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.share.views.adapter.ShareTeamsAdapter;
import com.tosslab.jandi.app.ui.share.views.model.ShareSelectModel;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by tee on 15. 9. 15..
 */

@EActivity(R.layout.activity_select_team)
public class ShareSelectTeamActivity extends BaseAppCompatActivity implements ShareTeamsAdapter.OnItemClickListener {

    @ViewById(R.id.lv_select_team)
    RecyclerView lvSelectTeam;

    @Bean
    ShareSelectModel shareSelectModel;

    ShareTeamsAdapter adapter;

    @AfterInject
    void initObject() {
        adapter = new ShareTeamsAdapter();
    }

    @AfterViews
    void initViews() {
        setupActionbar();
        lvSelectTeam.setLayoutManager(new LinearLayoutManager(this));
        lvSelectTeam.setAdapter(adapter);
        initTeams();
        adapter.setOnItemClickListener(this);
    }

    void setupActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setTitle(R.string.jandi_share_to_jandi);
    }

    @Background
    void initTeams() {
        try {
            List<Team> teams = shareSelectModel.getTeamInfos();
            ResAccountInfo.UserTeam selectedTeam = shareSelectModel.getSelectedTeamInfo();
            for (Team team : teams) {
                if (selectedTeam != null && selectedTeam.getTeamId() == team.getTeamId()) {
                    team.setSelected(true);
                }
            }
            showTeamList(teams);
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showTeamList(List<Team> teams) {
        adapter.setItems(teams);
        adapter.notifyDataSetChanged();
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

    @Override
    public void onItemClick(long teamId, String teamName) {
        ShareSelectTeamEvent event = new ShareSelectTeamEvent();
        event.setTeamId(teamId);
        event.setTeamName(teamName);
        EventBus.getDefault().post(event);
        finish();
    }

}
