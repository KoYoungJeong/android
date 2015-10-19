package com.tosslab.jandi.app.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.ConfirmDeleteTopicEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 14. 11. 10..
 */
public class DeleteTopicDialogFragment extends DialogFragment {

    public static DeleteTopicDialogFragment newInstance() {
        return new DeleteTopicDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(R.string.jandi_topic_ask_about_deleting)
                .setPositiveButton(R.string.jandi_action_delete,
                        (dialog, whichButton) -> EventBus.getDefault().post(new ConfirmDeleteTopicEvent()))
                .setNegativeButton(R.string.jandi_cancel, null)
                .create();
    }
}
