package com.tosslab.jandi.app.dialogs;

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.team.invite.TeamInvitationsEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by Bill Minwook Heo on 15. 4. 21..
 */
public class InvitationDialogFragment extends DialogFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Dialog me = getDialog();
        me.setCanceledOnTouchOutside(true);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_file_upload)
                .setItems(R.array.types_invitations, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int eventType;
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
                            case 6:     // from Copy Link
                            default:
                                eventType = JandiConstants.TYPE_INVITATION_COPY_LINK;
                                break;

                        }
                        EventBus.getDefault().post(new TeamInvitationsEvent(eventType));
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
}

