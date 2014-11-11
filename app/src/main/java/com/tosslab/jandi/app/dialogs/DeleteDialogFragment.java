package com.tosslab.jandi.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.ConfirmDeleteEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 14. 11. 10..
 */
public class DeleteDialogFragment extends DialogFragment {
    public static DeleteDialogFragment newInstance() {
        DeleteDialogFragment frag = new DeleteDialogFragment();
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.jandi_message_ask_about_deleting)
                .setPositiveButton(R.string.jandi_action_delete,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                EventBus.getDefault().post(new ConfirmDeleteEvent());
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
