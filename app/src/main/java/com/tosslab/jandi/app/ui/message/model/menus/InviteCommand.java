package com.tosslab.jandi.app.ui.message.model.menus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.entities.UnjoinedUserListAdapter;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
@EBean
class InviteCommand implements MenuCommand {

    private static final Logger log = Logger.getLogger(InviteCommand.class);

    private Activity activity;
    private JandiEntityClient mJandiEntityClient;
    private ChattingInfomations chattingInfomations;
    private EntityManager entityManager;

    void initData(Activity activity, JandiEntityClient mJandiEntityClient, ChattingInfomations chattingInfomations) {
        this.activity = activity;
        this.mJandiEntityClient = mJandiEntityClient;
        this.chattingInfomations = chattingInfomations;
        entityManager = EntityManager.getInstance(activity);
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
        /**
         * 사용자 초대를 위한 Dialog 를 보여준 뒤, 체크된 사용자를 초대한다.
         */
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_select_cdp, null);
        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);

        // 현재 채널에 가입된 사용자를 제외한 초대 대상 사용자 리스트를 획득한다.
        List<FormattedEntity> unjoinedMembers = entityManager.getUnjoinedMembersOfEntity(
                chattingInfomations.entityId,
                chattingInfomations.entityType);

        if (unjoinedMembers.size() <= 0) {
            ColoredToast.showWarning(activity, activity.getString(R.string.warn_all_users_are_already_invited));
            return;
        }

        final UnjoinedUserListAdapter adapter = new UnjoinedUserListAdapter(activity, unjoinedMembers);
        lv.setAdapter(adapter);

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
            inviteSucceed(invitedUsers.size());
        } catch (JandiNetworkException e) {
            log.error("fail to invite entity");
            inviteFailed(activity.getString(R.string.err_entity_invite));
        }
    }

    @UiThread
    public void inviteSucceed(int memberSize) {
        String rawString = activity.getString(R.string.jandi_message_invite_entity);
        String formatString = String.format(rawString, memberSize);

        ((BaseAnalyticsActivity) activity).trackInvitingToEntity(entityManager, chattingInfomations.entityType);
        ColoredToast.show(activity, formatString);
    }

    @UiThread
    public void inviteFailed(String errMessage) {
        ColoredToast.showError(activity, errMessage);
    }


}
