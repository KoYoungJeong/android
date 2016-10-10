package com.tosslab.jandi.app.ui.message.v2.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.DummyDeleteEvent;
import com.tosslab.jandi.app.events.messages.DummyRetryEvent;

import de.greenrobot.event.EventBus;

public class DummyMessageDialog extends DialogFragment {

    @InjectExtra
    long localId;

    public static void showDialog(FragmentManager fragmentManager, long localId) {

        DummyMessageDialog dummyMessageDialog = new DummyMessageDialog();
        Bundle args = new Bundle();
        args.putLong("localId", localId);
        dummyMessageDialog.setArguments(args);
        dummyMessageDialog.show(fragmentManager, "dialog");

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        Dart.inject(this, getArguments());


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.JandiTheme_AlertDialog_FixWidth_280);

        String[] items = {getString(R.string.jandi_try_again), getString(R.string.menu_entity_delete)};
        builder.setItems(items, (dialog, which) -> {
            switch (which) {
                case 0:
                    EventBus.getDefault().post(new DummyRetryEvent(localId));
                    break;
                case 1:
                    EventBus.getDefault().post(new DummyDeleteEvent(localId));
                    break;
            }
        });


        return builder.create();
    }
}
