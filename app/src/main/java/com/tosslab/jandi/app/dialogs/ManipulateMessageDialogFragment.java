package com.tosslab.jandi.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.ConfirmDeleteMessageEvent;
import com.tosslab.jandi.app.events.ReqModifyMessageEvent;
import com.tosslab.jandi.app.lists.MessageItem;
import com.tosslab.jandi.app.utils.DateTransformator;

import org.apache.log4j.Logger;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 5. 28..
 */
public class ManipulateMessageDialogFragment extends DialogFragment {
    private final Logger log = Logger.getLogger(ManipulateMessageDialogFragment.class);

    public static ManipulateMessageDialogFragment newInstance(MessageItem item) {

        String title = DateTransformator.getTimeString(item.getLinkTime());

        ManipulateMessageDialogFragment frag = new ManipulateMessageDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("messageId", item.getMessageId());
        args.putInt("messageType", item.getContentType());
        if (item.getContentType() == MessageItem.TYPE_COMMENT) {
            args.putInt("feedbackId", item.getFeedbackId());
        }
        args.putString("currentMessage", item.getContentString());
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        // 키보드 강제로 올리기
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String title = getArguments().getString("title", "");
        final int messageId = getArguments().getInt("messageId");
        final int feedbackId = getArguments().getInt("feedbackId", -1);
        final int messageType = getArguments().getInt("messageType");
        final String currentMessage = getArguments().getString("currentMessage");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_manipulate_message, null);

        // Edit 메뉴 클릭시.
        final TextView actionEdit = (TextView)mainView.findViewById(R.id.txt_action_edit_message);
        actionEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new ReqModifyMessageEvent(messageType, messageId, currentMessage, feedbackId));
                dismiss();
            }
        });

        // Delete 메뉴 클릭시.
        final TextView actionDel = (TextView)mainView.findViewById(R.id.txt_action_del_message);
        actionDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new ConfirmDeleteMessageEvent(messageType, messageId, feedbackId));
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
