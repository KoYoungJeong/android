package com.tosslab.jandi.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.ConfirmShareEvent;

import org.apache.log4j.Logger;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 7. 15..
 */
public class SelectCdpDialogNewFragment extends DialogFragment {
    private final Logger log = Logger.getLogger(SelectCdpDialogNewFragment.class);

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        LayoutInflater inflater = getActivity().getLayoutInflater();
//        View mainView = inflater.inflate(R.layout.dialog_select_cdp, null);
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setView(mainView)
//                .setIcon(android.R.drawable.ic_menu_agenda)
//                .setTitle(R.string.title_cdp_to_be_shared)
//                .setPositiveButton(R.string.share,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                EventBus.getDefault().post(
//                                        new ConfirmShareEvent(selectedCdpIdToBeShared));
//                                dismiss();
//                            }
//                        }
//                );
//
//        return builder.create();
        return super.onCreateDialog(savedInstanceState);
    }

}
