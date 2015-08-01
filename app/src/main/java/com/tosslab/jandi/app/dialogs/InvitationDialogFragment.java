package com.tosslab.jandi.app.dialogs;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.invites.email.InviteEmailActivity_;

/**
 * Created by Bill Minwook Heo on 15. 4. 21..
 */

public class InvitationDialogFragment extends DialogFragment {

    private static final String INVITE_TEAM_NAME = "invite_team_name";
    private static final String INVITATION_URL = "invite_url";

    ClipboardManager clipboardManager;

    public static InvitationDialogFragment newInstance(String inviteTeamName, String invitationUrl) {
        InvitationDialogFragment fragment = new InvitationDialogFragment();
        Bundle args = new Bundle();
        args.putString(INVITE_TEAM_NAME, inviteTeamName);
        args.putString(INVITATION_URL, invitationUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Dialog me = getDialog();
        me.setCanceledOnTouchOutside(true);
        clipboardManager = (ClipboardManager) getActivity()
                .getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.jandi_invite_member)
                .setItems(R.array.types_invitations, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int eventType = 0;
                        switch (which) {
                            case 0:     // from email
                                eventType = JandiConstants.TYPE_INVITATION_EMAIL;
                                break;
                            case 1:     // from kakao
                                eventType = JandiConstants.TYPE_INVITATION_KAKAO;
                                break;
                            case 2:     // from LINE
                                eventType = JandiConstants.TYPE_INVITATION_LINE;
                                break;
                            case 3:     // from WeChat
                                eventType = JandiConstants.TYPE_INVITATION_WECHAT;
                                break;
                            case 4:     // from Facebook Messenger
                                eventType = JandiConstants.TYPE_INVITATION_FACEBOOK_MESSENGER;
                                break;
                            case 5:     // from Copy Link
                                eventType = JandiConstants.TYPE_INVITATION_COPY_LINK;
                                break;

                        }
                        startInvitation(eventType);
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.jandi_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do Nothing
                            }
                        }
                );
        return builder.create();
    }

    private void startInvitation(int eventType) {
        if (eventType == JandiConstants.TYPE_INVITATION_COPY_LINK) {
            copyLink();
            showTextDialog(getActivity().getResources().getString(R.string.jandi_invite_succes_copy_link));
        } else {
            Intent intent = getInviteIntent(eventType);
            try {
                getActivity().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                copyLink();
                showTextDialog(getActivity().getApplicationContext()
                        .getResources().getString(R.string.jandi_invite_app_not_installed));
            }
        }
    }

    private Intent getInviteIntent(int eventType) {

        String publicLink = getArguments().getString(INVITATION_URL);
        String packageName;

        switch (eventType) {
            case JandiConstants.TYPE_INVITATION_KAKAO:
                packageName = JandiConstants.INVITE_URL_KAKAO;
                break;
            case JandiConstants.TYPE_INVITATION_LINE:
                packageName = JandiConstants.INVITE_URL_LINE;
                break;
            case JandiConstants.TYPE_INVITATION_WECHAT:
                packageName = JandiConstants.INVITE_URL_WECHAT;
                break;
            case JandiConstants.TYPE_INVITATION_FACEBOOK_MESSENGER:
                packageName = JandiConstants.INVITE_URL_FACEBOOK_MESSENGER;
                break;
            default:
            case JandiConstants.TYPE_INVITATION_EMAIL:
                return InviteEmailActivity_
                        .intent(getActivity())
                        .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP).get();
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setPackage(packageName);
        intent.putExtra(Intent.EXTRA_TEXT, getInvitationContents() + "\n" + publicLink);
        intent.setType("text/plain");
        if (packageName.equals(JandiConstants.INVITE_URL_FACEBOOK_MESSENGER)) {
            intent.putExtra(JandiConstants.INVITE_FACEBOOK_EXTRA_PROTOCOL_VERSION,
                    JandiConstants.INVITE_FACEBOOK_PROTOCOL_VERSION);
            intent.putExtra(JandiConstants.INVITE_FACEBOOK_EXTRA_APP_ID,
                    JandiConstants.INVITE_FACEBOOK_REGISTRATION_APP_ID);
            intent.setType("image/*");
        }

        return intent;

    }

    private void copyLink() {
        ClipData clipData = ClipData.newPlainText("",
                getInvitationContents() + "\n" + getArguments().getString(INVITATION_URL));
        clipboardManager.setPrimaryClip(clipData);
    }

    public void showTextDialog(String alertText) {
        new android.app.AlertDialog.Builder(getActivity())
                .setMessage(alertText)
                .setCancelable(false)
                .setPositiveButton(getActivity().getApplicationContext()
                                .getResources().getString(R.string.jandi_confirm),
                        (dialog, id) -> dialog.dismiss())
                .create().show();
    }

    public String getInvitationContents() {
        return getArguments().getString(INVITE_TEAM_NAME) +
                getActivity().getApplicationContext().getResources().getString(R.string.jandi_invite_contents);
    }

}