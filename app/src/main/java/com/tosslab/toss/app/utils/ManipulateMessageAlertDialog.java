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
import com.tosslab.toss.app.events.ConfirmDeleteMessageEvent;
import com.tosslab.toss.app.events.ReqModifyMessageEvent;
import com.tosslab.toss.app.navigation.MessageItem;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 5. 28..
 */
public class ManipulateMessageAlertDialog extends DialogFragment {
    public static ManipulateMessageAlertDialog newInstance(MessageItem item) {

        String title = DateTransformator.getTimeString(item.createTime);

        ManipulateMessageAlertDialog frag = new ManipulateMessageAlertDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("messageId", item.id);
        args.putString("currentMessage", item.contentString);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String title = getArguments().getString("title", "");
        final int messageId = getArguments().getInt("messageId");
        final String currentMessage = getArguments().getString("currentMessage");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_manipulate_message, null);

        // Edit 메뉴 클릭시.
        final TextView actionEdit = (TextView)mainView.findViewById(R.id.txt_action_edit_message);
        actionEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new ReqModifyMessageEvent(messageId, currentMessage));
                dismiss();
            }
        });

        // Delete 메뉴 클릭시.
        final TextView actionDel = (TextView)mainView.findViewById(R.id.txt_action_del_message);
        actionDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new ConfirmDeleteMessageEvent(messageId));
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
