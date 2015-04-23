package com.tosslab.jandi.app.ui.message.v2.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.DummyDeleteEvent;
import com.tosslab.jandi.app.events.messages.DummyRetryEvent;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 2. 5..
 */
@EFragment
public class DummyMessageDialog extends DialogFragment {

    @FragmentArg
    long localId;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String[] items = {getString(R.string.jandi_try_again), getString(R.string.menu_entity_delete)};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        EventBus.getDefault().post(new DummyRetryEvent(localId));
                        break;
                    case 1:
                        EventBus.getDefault().post(new DummyDeleteEvent(localId));
                        break;
                }
            }
        })
                .setNegativeButton(R.string.jandi_cancel, null);


        return builder.create();
    }
}
