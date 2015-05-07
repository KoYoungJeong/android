package com.tosslab.jandi.app.ui.message.model.menus;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.InvitationDialogFragment;
import com.tosslab.jandi.app.events.team.invite.TeamInvitationsEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.entities.UnjoinedUserListAdapter;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.ui.invites.InviteUtils;
import com.tosslab.jandi.app.ui.maintab.topic.model.EntityComparator;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
@EBean
class InviteCommand implements MenuCommand {

    private static final Logger log = Logger.getLogger(InviteCommand.class);

    private AppCompatActivity activity;
    private JandiEntityClient mJandiEntityClient;
    private ChattingInfomations chattingInfomations;
    private EntityManager entityManager;

    private ProgressWheel progressWheel;

    @Bean
    TeamDomainInfoModel teamDomainInfoModel;
    private String invitationUrl;
    private String teamName;

    @SystemService
    ClipboardManager clipboardManager;

    void initData(AppCompatActivity activity, JandiEntityClient mJandiEntityClient, ChattingInfomations chattingInfomations) {
        this.activity = activity;
        this.mJandiEntityClient = mJandiEntityClient;
        this.chattingInfomations = chattingInfomations;
        entityManager = EntityManager.getInstance(activity);

        progressWheel = new ProgressWheel(activity);
        progressWheel.init();
    }

    @Override
    public void execute(MenuItem menuItem) {
        inviteMembersToEntity();
    }

    /**
     * Channel, PrivateGroup Invite
     */
    @UiThread
    public void inviteMembersToEntity() {

        int teamMemberCountWithoutMe = entityManager.getFormattedUsersWithoutMe().size();

        if (teamMemberCountWithoutMe <= 0) {
//            InviteActivity_.intent(activity)
//                    .start();
            onInvitationDisableCheck();
            return;

        }

        /**
         * 사용자 초대를 위한 Dialog 를 보여준 뒤, 체크된 사용자를 초대한다.
         */
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_select_cdp, null);
        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);

        // 현재 채널에 가입된 사용자를 제외한 초대 대상 사용자 리스트를 획득한다.
        List<FormattedEntity> unjoinedMembers = getUnjoinedEntities();

        if (unjoinedMembers.size() <= 0) {
            ColoredToast.showWarning(activity, activity.getString(R.string.warn_all_users_are_already_invited));
            return;
        }
        Collections.sort(unjoinedMembers, new EntityComparator());

        final UnjoinedUserListAdapter adapter = new UnjoinedUserListAdapter(activity, unjoinedMembers);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UnjoinedUserListAdapter userListAdapter = (UnjoinedUserListAdapter) parent.getAdapter();
                FormattedEntity item = userListAdapter.getItem(position);
                item.isSelectedToBeJoined = !item.isSelectedToBeJoined;

                userListAdapter.notifyDataSetChanged();
            }
        });

        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(R.string.title_cdp_invite);
        dialog.setView(view);
        dialog.setPositiveButton(R.string.menu_entity_invite, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                List<Integer> selectedCdp = adapter.getSelectedUserIds();
                if (selectedCdp != null && !selectedCdp.isEmpty()) {
                    inviteInBackground(selectedCdp);
                } else {
                    inviteFailed(activity.getString(R.string.title_cdp_invite));
                }
            }
        });
        dialog.show();
    }

    private List<FormattedEntity> getUnjoinedEntities() {
        List<FormattedEntity> unjoinedMembersOfEntity = entityManager.getUnjoinedMembersOfEntity(
                chattingInfomations.entityId,
                chattingInfomations.entityType);

        for (int idx = unjoinedMembersOfEntity.size() - 1; idx >= 0; idx--) {
            FormattedEntity formattedEntity = unjoinedMembersOfEntity.get(idx);
            if (!TextUtils.equals(formattedEntity.getUser().status, "enabled")) {
                unjoinedMembersOfEntity.remove(idx);
            }
        }

        return unjoinedMembersOfEntity;
    }

    @Background
    public void inviteInBackground(List<Integer> invitedUsers) {
        try {
            if (chattingInfomations.isPublicTopic()) {
                mJandiEntityClient.inviteChannel(
                        chattingInfomations.entityId, invitedUsers);
            } else if (chattingInfomations.isPrivateTopic()) {
                mJandiEntityClient.invitePrivateGroup(
                        chattingInfomations.entityId, invitedUsers);
            }

            ResLeftSideMenu resLeftSideMenu = mJandiEntityClient.getTotalEntitiesInfo();
            JandiEntityDatabaseManager.getInstance(activity).upsertLeftSideMenu(resLeftSideMenu);
            EntityManager.getInstance(activity).refreshEntity(resLeftSideMenu);


            inviteSucceed(invitedUsers.size());
        } catch (JandiNetworkException e) {
            log.error("fail to invite entity");
            inviteFailed(activity.getString(R.string.err_entity_invite));
        } catch (Exception e) {
            inviteFailed(activity.getString(R.string.err_entity_invite));
        }
    }

    @UiThread
    public void inviteSucceed(int memberSize) {
        String rawString = activity.getString(R.string.jandi_message_invite_entity);
        String formatString = String.format(rawString, memberSize);

        ColoredToast.show(activity, formatString);
    }

    @UiThread
    public void inviteFailed(String errMessage) {
        ColoredToast.showError(activity, errMessage);
    }

    @Background
    public void onInvitationDisableCheck() {
        showProgressWheel();

        Pair<InviteUtils.Result, ResTeamDetailInfo.InviteTeam> result =
                InviteUtils.checkInvitationDisabled(teamDomainInfoModel, entityManager.getTeamId());

        dismissProgressWheel();

        switch (result.first) {
            case NETWORK_ERROR:
                showErrorToast(activity.getResources().getString(R.string.err_network));
                break;
            case ERROR:
                showErrorToast(activity.getResources().getString(R.string.err_entity_invite));
                break;
            case INVITATION_DISABLED:
                showTextDialog(
                        activity.getResources().getString(R.string.jandi_invite_disabled, getOwnerName()));
                break;
            case UNDEFINED_URL:
                showErrorToast(activity.getResources().getString(R.string.err_entity_invite));
                break;
            case SUCCESS:
                moveToInvitationActivity(result.second);
                break;
            default:
                break;
        }
    }

    private String getOwnerName() {
        List<FormattedEntity> users = EntityManager.getInstance(activity).getFormattedUsers();
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
        invitationDialog.show(activity.getSupportFragmentManager(), "invitationsDialog");
    }

    public void onEvent(TeamInvitationsEvent event) {
        String invitationContents =
                teamName + activity.getResources().getString(R.string.jandi_invite_contents);
        int eventType = event.type;
        if (eventType == JandiConstants.TYPE_INVITATION_COPY_LINK) {
            copyLink(invitationUrl, invitationContents);
            showTextDialog(activity.getResources().getString(R.string.jandi_invite_succes_copy_link));
        } else {
            Intent intent = InviteUtils.getInviteIntent(
                    activity, event, invitationUrl, invitationContents);
            try {
                activity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                copyLink(invitationUrl, invitationContents);
                showTextDialog(activity.getResources().getString(R.string.jandi_invite_app_not_installed));
            }
        }
    }

    public void copyLink(String publicLink, String contents) {
        ClipData clipData = ClipData.newPlainText("", contents + "\n" + publicLink);
        clipboardManager.setPrimaryClip(clipData);
    }

    @UiThread
    void showErrorToast(String message) {
        ColoredToast.showError(activity, message);
    }

    @UiThread
    public void showTextDialog(String alertText) {
        new AlertDialog.Builder(activity)
                .setMessage(alertText)
                .setCancelable(false)
                .setPositiveButton(activity.getResources().getString(R.string.jandi_confirm),
                        (dialog, id) -> dialog.dismiss())
                .create().show();
    }

    @UiThread
    public void showProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

        if (progressWheel != null) {
            progressWheel.show();
        }
    }

    @UiThread
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }
}
