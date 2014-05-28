package com.tosslab.toss.app.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tosslab.toss.app.R;
import com.tosslab.toss.app.events.DeleteCdpEvent;
import com.tosslab.toss.app.events.DeleteMessageEvent;
import com.tosslab.toss.app.events.EditCdpEvent;
import com.tosslab.toss.app.events.EditMessageEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 5. 28..
 */
public class ManipulateCdpAlertDialog extends DialogFragment {
    public static ManipulateCdpAlertDialog newInstance(String title, int cdpId, int cdpType) {
        ManipulateCdpAlertDialog frag = new ManipulateCdpAlertDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("cdpId", cdpId);
        args.putInt("cdpType", cdpType);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String title = getArguments().getString("title", "");
        final int cdpId = getArguments().getInt("cdpId");
        final int cdpType = getArguments().getInt("cdpType");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_manipulate_cdp, null);

        // Edit 메뉴 클릭시.
        final TextView actionEdit = (TextView)mainView.findViewById(R.id.txt_action_edit_cdp);
        actionEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new EditCdpEvent(cdpId, cdpType));
                dismiss();
            }
        });

        // Delete 메뉴 클릭시.
        final TextView actionDel = (TextView)mainView.findViewById(R.id.txt_action_del_cdp);
        actionDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new DeleteCdpEvent(cdpId, cdpType));
                dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mainView)
                .setIcon(android.R.drawable.ic_menu_agenda)
                .setTitle(title)
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do Nothing
                            }
                        }
                );
        return builder.create();
    }
}
