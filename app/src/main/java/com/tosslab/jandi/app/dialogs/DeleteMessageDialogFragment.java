package com.tosslab.jandi.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.ConfirmDeleteMessageEvent;
import com.tosslab.jandi.app.events.messages.RequestDeleteMessageEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 14. 11. 20..
 */
public class DeleteMessageDialogFragment extends DialogFragment {
    private static final String MESSAGE_ID  = "messageId";
    private static final String MESSAGE_TYPE    = "messageType";
    private static final String FEEDBACK_ID     = "feedbackId";

    public static DeleteMessageDialogFragment newInstance(RequestDeleteMessageEvent req) {
        DeleteMessageDialogFragment frag = new DeleteMessageDialogFragment();
        Bundle args = new Bundle();
        args.putInt(MESSAGE_ID, req.messageId);
        args.putInt(MESSAGE_TYPE, req.messageType);
        args.putInt(FEEDBACK_ID, req.feedbackId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int messageId = getArguments().getInt(MESSAGE_ID);
        final int messageType = getArguments().getInt(MESSAGE_TYPE);

        final int feedbackId = getArguments().getInt(FEEDBACK_ID, -1);

        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.jandi_message_ask_about_deleting)
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
                .setNegativeButton(R.string.jandi_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // DO NOTHING
                            }
                        }
                )
                .create();
    }
}
