package com.tosslab.jandi.app.dialogs;

import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.tosslab.jandi.app.R;

/**
 * Created by Bill Minwook Heo on 15. 4. 30..
 */
public class TextDialog {

    Context context;

    public TextDialog(Context context) {
        this.context = context;
    }

    public void showDialog(String alertText) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder
                .setMessage(alertText)
                .setCancelable(false)
                .setNegativeButton(context.getString(R.string.jandi_confirm),
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                // 다이얼로그를 취소한다
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }
}
