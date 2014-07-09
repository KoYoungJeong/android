package com.tosslab.jandi.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.DeleteCdpEvent;
import com.tosslab.jandi.app.events.ModifyCdpEvent;
import com.tosslab.jandi.app.lists.CdpItem;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 5. 28..
 */
public class ManipulateCdpDialogFragment extends DialogFragment {
    public static ManipulateCdpDialogFragment newInstance(CdpItem item) {
        ManipulateCdpDialogFragment frag = new ManipulateCdpDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", item.name);
        args.putInt("cdpId", item.id);
        args.putInt("cdpType", item.type);
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
                EventBus.getDefault().post(new ModifyCdpEvent(cdpId, cdpType, title));
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
