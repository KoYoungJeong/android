package com.tosslab.jandi.app.ui.invites;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.invites.emails.InviteEmailActivity;
import com.tosslab.jandi.app.ui.invites.member.MemberInvitationActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import java.util.List;

import rx.Observable;

/**
 * Created by tee on 2016. 12. 15..
 */

public class InviteDialogExecutor {

    private static InviteDialogExecutor inviteDialogExecutor;

    private InviteDialogExecutor() {
    }

    public static InviteDialogExecutor getInstance() {
        if (inviteDialogExecutor == null) {
            inviteDialogExecutor = new InviteDialogExecutor();
        }
        return inviteDialogExecutor;
    }

    private static AvailableState availableState(String invitationStatus, String invitationUrl) {
        if (!TextUtils.isEmpty(invitationUrl) && invitationUrl.contains("undefined")) {
            return AvailableState.UNDEFINE;
        }
        if (TextUtils.isEmpty(invitationStatus) || TextUtils.equals(invitationStatus, "disabled")) {
            return AvailableState.DISABLE;
        }
        return AvailableState.AVAIL;
    }

    public static boolean canBeInviation(String invitationStatus, String invitationUrl) {
        return availableState(invitationStatus, invitationUrl) == AvailableState.AVAIL;
    }

    public void executeInvite(Context context) {
        try {
            TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();
            boolean teamOwner = teamInfoLoader.getUser(teamInfoLoader.getMyId()).isTeamOwner();
            String invitationStatus = teamInfoLoader.getInvitationStatus();
            String invitationUrl = teamInfoLoader.getInvitationUrl();
            AvailableState availableState = availableState(invitationStatus, invitationUrl);
            switch (availableState) {
                case AVAIL:
                    showInvitationDialog(context, false);
                    break;
                case UNDEFINE:
                    if (!teamOwner) {
                        showErrorToast(JandiApplication.getContext()
                                .getString(R.string.err_entity_invite));
                    } else {
                        showInvitationDialog(context, true);
                    }
                    break;
                case DISABLE:
                    if (!teamOwner) {
                        showErrorInviteDisabledDialog(context);
                    } else {
                        showInvitationDialog(context, true);
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorToast(JandiApplication.getContext().getResources().getString(R.string.err_entity_invite));
        }
    }

    public void showInvitationDialog(Context context, boolean isNotAvailButTeamOwner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.JandiTheme_AlertDialog_FixWidth_300);

        android.view.View view = LayoutInflater.from(context).inflate(R.layout.dialog_invitation_user, null);

        AlertDialog invitationDialog = builder
                .setTitle(JandiApplication.getContext().getString(R.string.invite_member_option_title))
                .setView(view)
                .setNegativeButton(context.getResources().getString(R.string.jandi_cancel),
                        (dialog, id) -> dialog.dismiss())
                .create();

        view.findViewById(R.id.vg_invite_associate)
                .setOnClickListener(v -> {
                    if (hasNonDefaultTopic()) {
                        InviteEmailActivity.startActivityForAssociate(context);
                        invitationDialog.dismiss();
                    } else {
                        showErrorNotAvailableInviteTopicDialog(context);
                    }
                });

        view.findViewById(R.id.vg_invite_member)
                .setOnClickListener(v -> {
                    if (isNotAvailButTeamOwner) {
                        ColoredToast.showGray(R.string.jandi_invitation_for_admin);
                        InviteEmailActivity.startActivityForMember(context);
                    } else {
                        Intent intent = new Intent(context, MemberInvitationActivity.class);
                        context.startActivity(intent);
                    }
                    invitationDialog.dismiss();
                });

        invitationDialog.show();
    }

    private boolean hasNonDefaultTopic() {
        return Observable.from(TeamInfoLoader.getInstance().getTopicList())
                .filter(topic -> topic.isJoined())
                .filter(topic -> !topic.isDefaultTopic())
                .count()
                .map(cnt -> cnt > 0)
                .toBlocking()
                .firstOrDefault(false);
    }

    private void showErrorToast(String message) {
        ColoredToast.showError(message);
    }

    private void showErrorInviteDisabledDialog(Context context) {
        new AlertDialog.Builder(context, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(JandiApplication.getContext()
                        .getString(R.string.jandi_invite_disabled, getOwnerName()))
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.jandi_confirm),
                        (dialog, id) -> {
                            AnalyticsUtil.sendEvent(
                                    AnalyticsValue.Screen.TeamTab,
                                    AnalyticsValue.Action.InviteMember_InviteDisabled);
                            dialog.dismiss();
                        })
                .create().show();
    }

    private String getOwnerName() {
        List<User> users = TeamInfoLoader.getInstance().getUserList();
        return Observable.from(users)
                .takeFirst(user -> user.getLevel() == Level.Owner)
                .map(User::getName)
                .toBlocking()
                .firstOrDefault("");
    }

    private void showErrorNotAvailableInviteTopicDialog(Context context) {
        new AlertDialog.Builder(context, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setTitle(JandiApplication.getContext()
                        .getString(R.string.invite_associate_invitoronlyindefault_title))
                .setMessage(JandiApplication.getContext()
                        .getString(R.string.invite_associate_invitoronlyindefault_desc))
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.jandi_confirm),
                        (dialog, id) -> {
                        })
                .create().show();
    }

    private enum AvailableState {
        AVAIL, UNDEFINE, DISABLE
    }

}
