package com.tosslab.jandi.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.tosslab.jandi.app.R;

/**
 * Created by Bill Minwook Heo on 15. 4. 28..
 */
public class TextViewDialogFragment extends DialogFragment {

    private String alertText;

    public TextViewDialogFragment(String alertText) {
        this.alertText = alertText;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Dialog me = getDialog();
        me.setCanceledOnTouchOutside(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(alertText)
                .setNegativeButton(R.string.jandi_confirm,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do Nothing
                            }
                        }
                );

        return builder.create();
    }
}
