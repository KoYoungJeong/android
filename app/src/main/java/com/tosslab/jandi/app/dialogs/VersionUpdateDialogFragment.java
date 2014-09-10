package com.tosslab.jandi.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.tosslab.jandi.app.R;

/**
 * Created by justinygchoi on 2014. 9. 10..
 */
public class VersionUpdateDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.jandi_update_title)
                .setMessage(R.string.jandi_update_message)
                .setPositiveButton(R.string.jandi_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                final String appPackageName = getActivity().getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW,
                                            Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW,
                                            Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                            }
                        }
                );
        return builder.create();
    }
}
