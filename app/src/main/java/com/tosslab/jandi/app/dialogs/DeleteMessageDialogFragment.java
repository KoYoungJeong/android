package com.tosslab.jandi.app.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.ConfirmDeleteMessageEvent;
import com.tosslab.jandi.app.events.messages.RequestDeleteMessageEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 14. 11. 20..
 */
public class DeleteMessageDialogFragment extends DialogFragment {
    private static final String MESSAGE_ID = "messageId";
    private static final String MESSAGE_TYPE = "messageType";
    private static final String FEEDBACK_ID = "feedbackId";
    private static final String IS_COMMENT = "is_comment";

    public static DeleteMessageDialogFragment newInstance(RequestDeleteMessageEvent req, boolean isComment) {
        DeleteMessageDialogFragment frag = new DeleteMessageDialogFragment();
        Bundle args = new Bundle();
        args.putInt(MESSAGE_ID, req.messageId);
        args.putInt(MESSAGE_TYPE, req.messageType);
        args.putInt(FEEDBACK_ID, req.feedbackId);
        args.putBoolean(IS_COMMENT, isComment);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int messageId = getArguments().getInt(MESSAGE_ID);
        final int messageType = getArguments().getInt(MESSAGE_TYPE);

        final int feedbackId = getArguments().getInt(FEEDBACK_ID, -1);
        final boolean isComment = getArguments().getBoolean(IS_COMMENT, false);

        String message;
        if (!isComment) {
            message = getString(R.string.jandi_message_ask_about_deleting);
        } else {
            message = getString(R.string.jandi_message_ask_about_delete_comment);
        }

        return new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(message)
                .setPositiveButton(R.string.jandi_action_delete,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                EventBus.getDefault().post(
                                        new ConfirmDeleteMessageEvent(
                                                messageType,
                                                messageId,
                                                feedbackId
                                        )
                                );
                            }
                        }
                )
                .setNegativeButton(R.string.jandi_cancel, null)
                .create();
    }
}
