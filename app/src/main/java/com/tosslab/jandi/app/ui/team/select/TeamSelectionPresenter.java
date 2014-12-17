package com.tosslab.jandi.app.ui.team.select;

import android.content.Context;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.team.TeamListAdapter;
import com.tosslab.jandi.app.ui.team.info.TeamDomainInfoActivity;
import com.tosslab.jandi.app.ui.team.info.TeamDomainInfoActivity_;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * Created by Steve SeongUg Jung on 14. 12. 17..
 */
@EBean
public class TeamSelectionPresenter {

    private final Logger log = Logger.getLogger(TeamSelectionPresenter.class);


    @RootContext
    Context context;

    @ViewById(R.id.lv_intro_team_list)
    ListView listViewTeamList;

    private int mSelectedTeamId;
    private int lastSelectedPosition;
    private ProgressWheel mProgressWheel;
    private TeamListAdapter teamListAdapter;

    @AfterViews
    void init() {

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(context);
        mProgressWheel.init();

        teamListAdapter = new TeamListAdapter(context);
        listViewTeamList.setAdapter(teamListAdapter);
        lastSelectedPosition = -1;
    }

    @ItemClick(R.id.lv_intro_team_list)
    void teamItemClick(int position) {

        Team selectedMyTeam = teamListAdapter.getItem(position);

        Team.Status status = selectedMyTeam.getStatus();

        switch (status) {
            case JOINED:
                selectedMyTeam.setSelected(true);
                if (lastSelectedPosition != -1) {
                    teamListAdapter.getItem(lastSelectedPosition).setSelected(false);
                }
                teamListAdapter.notifyDataSetChanged();
                mSelectedTeamId = selectedMyTeam.getTeamId();
                lastSelectedPosition = position;
                log.debug(selectedMyTeam.getName() + ", id=" + mSelectedTeamId + ", is selected : " + selectedMyTeam.isSelected());
                break;
            case PENDING:
                // nothing action
                break;
            case CREATE:
                // TODO create team action
                TeamDomainInfoActivity_.intent(context)
                        .mode(TeamDomainInfoActivity.Mode.Create.name())
                        .start();
                break;
        }


    }

    @UiThread
    public void setTeamList(ArrayList<Team> teamList) {

        for (Team userTeam : teamList) {
            teamListAdapter.add(userTeam);
        }

        // Create Team Row
        teamListAdapter.add(Team.createEmptyTeam());

        teamListAdapter.notifyDataSetChanged();
    }

    @UiThread
    public void showErrorToast(int statusCode, String errorMsg) {
        ColoredToast.showError(context, context.getString(R.string.err_network));
    }
}
