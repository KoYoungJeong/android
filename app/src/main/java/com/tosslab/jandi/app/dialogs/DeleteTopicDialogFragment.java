package com.tosslab.jandi.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.ConfirmDeleteTopicEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 14. 11. 10..
 */
public class DeleteTopicDialogFragment extends DialogFragment {

    public static DeleteTopicDialogFragment newInstance() {
        DeleteTopicDialogFragment frag = new DeleteTopicDialogFragment();
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.jandi_topic_ask_about_deleting)
                .setPositiveButton(R.string.jandi_action_delete,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                EventBus.getDefault().post(new ConfirmDeleteTopicEvent());
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
