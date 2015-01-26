package com.tosslab.jandi.app.ui.member;

import android.app.ActionBar;
import android.graphics.drawable.ColorDrawable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.profile.ProfileDetailEvent;
import com.tosslab.jandi.app.lists.FormattedDummyEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.invites.InviteActivity_;
import com.tosslab.jandi.app.ui.member.adapter.TeamMemberListAdapter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 9. 18..
 */
@EActivity(R.layout.activity_team_info)
public class TeamInfoActivity extends BaseAnalyticsActivity {

    private static final Logger log = Logger.getLogger(TeamInfoActivity.class);

    @ViewById(R.id.list_team_users)
    ListView listViewInvitation;


    @Bean
    TeamMemberListAdapter teamUserListAdapter;

    @Bean
    JandiEntityClient mJandiEntityClient;

    private ProgressWheel mProgressWheel;
    private EntityManager mEntityManager;

    @AfterViews
    public void initForm() {
        setUpActionBar();
        initProgressWheel();
        listViewInvitation.setAdapter(teamUserListAdapter);

        mEntityManager = EntityManager.getInstance(TeamInfoActivity.this);
        retrieveTeamUserList(mEntityManager.getFormattedUsers());
    }

    @SupposeUiThread
    void setUpActionBar() {
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    @SupposeUiThread
    void initProgressWheel() {
        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        if (mEntityManager != null) {
            trackGaTeamInfo(mEntityManager.getDistictId());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(ProfileDetailEvent event) {
        UserInfoDialogFragment_.builder().entityId(event.getEntityId()).build().show(getFragmentManager(), "dialog");
    }


    @Override
    protected void onStop() {
        if (mProgressWheel != null)
            mProgressWheel.dismiss();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.team_info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_invitation:
                InviteActivity_.intent(TeamInfoActivity.this)
                        .start();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void retrieveTeamUserList(List<FormattedEntity> users) {
        teamUserListAdapter.retrieveList(users);
    }

    /**
     * *********************************************************
     * 팀원으로 초대
     * **********************************************************
     */
    @UiThread
    public void inviteTeamMember(String email) {
        mProgressWheel.show();
        inviteTeamMemberInBackground(email);
    }

    @Background
    public void inviteTeamMemberInBackground(String email) {
        try {
            List<ResInvitationMembers> resInvitation = mJandiEntityClient.inviteTeamMember(email);
            inviteTeamMemberSucceed(resInvitation, email);
        } catch (JandiNetworkException e) {
            log.error("Invitation failed", e);
            inviteTeamMemberFailed(getString(R.string.err_invitation_failed));
        } catch (Exception e) {
            log.error("Invitation failed", e);
            inviteTeamMemberFailed(getString(R.string.err_invitation_failed));
        }
    }

    @UiThread
    public void inviteTeamMemberSucceed(List<ResInvitationMembers> resInvitation, String succeedEmail) {
        mProgressWheel.dismiss();
        ColoredToast.show(this, getString(R.string.jandi_invitation_succeed));
        for (ResInvitationMembers resInvitationMembers : resInvitation) {
            teamUserListAdapter.addMember(new FormattedDummyEntity(resInvitationMembers.getEmail()));
            if (mEntityManager != null) {
                trackInviteUser(mEntityManager.getDistictId());
            }
        }
    }

    @UiThread
    public void inviteTeamMemberFailed(String message) {
        mProgressWheel.dismiss();
        ColoredToast.showError(this, message);
    }
}
