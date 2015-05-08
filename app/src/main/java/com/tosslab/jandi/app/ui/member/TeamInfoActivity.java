package com.tosslab.jandi.app.ui.member;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.InvitationDialogFragment;
import com.tosslab.jandi.app.dialogs.profile.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.entities.MemberStarredEvent;
import com.tosslab.jandi.app.events.profile.ProfileDetailEvent;
import com.tosslab.jandi.app.events.team.invite.TeamInvitationsEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.invites.InviteUtils;
import com.tosslab.jandi.app.ui.member.adapter.TeamMemberListAdapter;
import com.tosslab.jandi.app.ui.member.model.TeamInfoModel;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;

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

    @Bean
    TeamInfoModel teamInfoModel;

    @Bean
    TeamDomainInfoModel teamDomainInfoModel;

    @SystemService
    ClipboardManager clipboardManager;

    private String invitationUrl;
    private String teamName;

    private ProgressWheel mProgressWheel;
    private EntityManager mEntityManager;

    @AfterViews
    public void initForm() {
        setUpActionBar();
        initProgressWheel();
        listViewInvitation.setAdapter(teamUserListAdapter);

        mEntityManager = EntityManager.getInstance(TeamInfoActivity.this);

        List<FormattedEntity> entities = teamInfoModel.retrieveTeamUserList();
        teamUserListAdapter.retrieveList(entities);
    }

    @SupposeUiThread
    void setUpActionBar() {
        // Set up the action bar.

        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
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
        UserInfoDialogFragment_.builder().entityId(event.getEntityId()).build().show(getSupportFragmentManager(), "dialog");
    }

    public void onEvent(final RequestMoveDirectMessageEvent event) {
        EntityManager entityManager = EntityManager.getInstance(TeamInfoActivity.this);
        MessageListV2Activity_.intent(TeamInfoActivity.this)
                .teamId(entityManager.getTeamId())
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(event.userId)
                .roomId(-1)
                .isFavorite(entityManager.getEntityById(event.userId).isStarred)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();
    }

    public void onEvent(MemberStarredEvent event) {
        List<FormattedEntity> entities = teamInfoModel.retrieveTeamUserList();
        teamUserListAdapter.retrieveList(entities);
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
                onInvitationDisableCheck();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Background
    public void onInvitationDisableCheck() {
        showProgressWheel();

        Pair<InviteUtils.Result, ResTeamDetailInfo.InviteTeam> result =
                InviteUtils.checkInvitationDisabled(teamDomainInfoModel, mEntityManager.getTeamId());

        dismissProgressWheel();

        switch (result.first) {
            case NETWORK_ERROR:
                showErrorToast(getResources().getString(R.string.err_network));
                break;
            case ERROR:
                showErrorToast(getResources().getString(R.string.err_entity_invite));
                break;
            case INVITATION_DISABLED:
                showTextDialog(
                        getResources().getString(R.string.jandi_invite_disabled, getOwnerName()));
                break;
            case UNDEFINED_URL:
                showErrorToast(getResources().getString(R.string.err_entity_invite));
                break;
            case SUCCESS:
                moveToInvitationActivity(result.second);
                break;
            default:
                break;
        }
    }

    private String getOwnerName() {
        List<FormattedEntity> users = EntityManager.getInstance(this).getFormattedUsers();
        FormattedEntity tempDefaultEntity = new FormattedEntity();
        FormattedEntity owner = Observable.from(users)
                .filter(formattedEntity ->
                        TextUtils.equals(formattedEntity.getUser().u_authority, "owner"))
                .firstOrDefault(tempDefaultEntity)
                .toBlocking()
                .first();
        return owner.getUser().name;
    }

    @UiThread
    public void moveToInvitationActivity(ResTeamDetailInfo.InviteTeam inviteTeam) {
        invitationUrl = inviteTeam.getInvitationUrl();
        teamName = inviteTeam.getName();
        DialogFragment invitationDialog = new InvitationDialogFragment();
        invitationDialog.show(getSupportFragmentManager(), "invitationsDialog");
    }

    @UiThread
    void showErrorToast(String message) {
        ColoredToast.showError(this, message);
    }

    @UiThread
    void dismissProgressWheel() {
        if (mProgressWheel != null && mProgressWheel.isShowing()) {
            mProgressWheel.dismiss();
        }
    }

    @UiThread
    void showProgressWheel() {
        if (mProgressWheel == null) {
            mProgressWheel = new ProgressWheel(this);
            mProgressWheel.init();
        }

        if (mProgressWheel != null && !mProgressWheel.isShowing()) {
            mProgressWheel.show();
        }
    }

    @UiThread
    public void showTextDialog(String alertText) {
        new AlertDialog.Builder(this)
                .setMessage(alertText)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.jandi_confirm),
                        (dialog, id) -> dialog.dismiss())
                .create().show();
    }

    public void onEvent(TeamInvitationsEvent event) {
        String invitationContents =
                teamName + getResources().getString(R.string.jandi_invite_contents);
        int eventType = event.type;
        if (eventType == JandiConstants.TYPE_INVITATION_COPY_LINK) {
            copyLink(invitationUrl, invitationContents);
            showTextDialog(getResources().getString(R.string.jandi_invite_succes_copy_link));
        } else {
            Intent intent = InviteUtils.getInviteIntent(this, event, invitationUrl, invitationContents);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                copyLink(invitationUrl, invitationContents);
                showTextDialog(getResources().getString(R.string.jandi_invite_app_not_installed));
            }
        }
    }

    public void copyLink(String publicLink, String contents) {
        ClipData clipData = ClipData.newPlainText("", contents + "\n" + publicLink);
        clipboardManager.setPrimaryClip(clipData);
    }
}
