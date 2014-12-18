package com.tosslab.jandi.app.ui.team.select;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.team.TeamListAdapter;
import com.tosslab.jandi.app.ui.login.IntroMainActivity_;
import com.tosslab.jandi.app.ui.team.info.TeamDomainInfoActivity;
import com.tosslab.jandi.app.ui.team.info.TeamDomainInfoActivity_;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.TokenUtil;

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
    Activity activity;

    @ViewById(R.id.lv_intro_team_list)
    ListView listViewTeamList;

    private int mSelectedTeamId;
    private int lastSelectedPosition;
    private ProgressWheel mProgressWheel;
    private TeamListAdapter teamListAdapter;

    @AfterViews
    void init() {

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(activity);
        mProgressWheel.init();

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
                lastSelectedPosition = position != lastSelectedPosition ? position : -1;
                log.debug(selectedMyTeam.getName() + ", id=" + mSelectedTeamId + ", is selected : " + selectedMyTeam.isSelected());

                activity.invalidateOptionsMenu();

                break;
            case PENDING:
                // nothing action
                break;
            case CREATE:
                // TODO create team action
                TeamDomainInfoActivity_.intent(activity)
                        .mode(TeamDomainInfoActivity.Mode.CREATE.name())
                        .startForResult(TeamSelectionActivity.REQ_TEAM_CREATE);
                break;
        }


    }

    @UiThread
    public void setTeamList(ArrayList<Team> teamList) {

        teamListAdapter = new TeamListAdapter(activity);
        listViewTeamList.setAdapter(teamListAdapter);

        for (Team userTeam : teamList) {
            teamListAdapter.add(userTeam);
        }

        // Create Team Row
        teamListAdapter.add(Team.createEmptyTeam());

        teamListAdapter.notifyDataSetChanged();
    }

    @UiThread
    public void showErrorToast(int statusCode, String errorMsg) {
        ColoredToast.showError(activity, activity.getString(R.string.err_network));
    }

    public void showLogoutDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.jandi_setting_sign_out)
                .setMessage("다른 아이디로 로그인하시겠습니까?")
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TokenUtil.clearTokenInfo(activity);

                        IntroMainActivity_.intent(activity)
                                .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                .startForResult(TeamSelectionActivity.REQ_TEAM_CREATE);

                        activity.finish();

                    }
                })
                .create().show();


    }

    @UiThread
    public void showProgressWheel() {
        dismissProgressWheel();

        mProgressWheel.show();
    }

    @UiThread
    public void dismissProgressWheel() {
        if (mProgressWheel != null && mProgressWheel.isShowing()) {
            mProgressWheel.dismiss();
        }
    }

    public int getLastSelectedPosition() {
        return lastSelectedPosition;
    }

    public void selectTeam(int lastSelectedPosition) {
        log.debug("Selected Team Id : " + lastSelectedPosition);
    }
}
