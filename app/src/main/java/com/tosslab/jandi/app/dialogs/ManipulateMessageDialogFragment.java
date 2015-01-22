package com.tosslab.jandi.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.ConfirmCopyMessageEvent;
import com.tosslab.jandi.app.events.messages.RequestDeleteMessageEvent;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 5. 28..
 */
public class ManipulateMessageDialogFragment extends DialogFragment {
    private static final String TITLE = "title";
    private static final String MESSAGE_ID = "messageId";
    private static final String MESSAGE_TYPE = "messageType";
    private static final String FEEDBACK_ID = "feedbackId";
    private static final String CURRENT_MESSAGE = "currentMessage";
    private static final String IS_MINE = "isMine";

    public static ManipulateMessageDialogFragment newInstance(MessageItem item) {
        return newInstance(item, false);
    }

    public static ManipulateMessageDialogFragment newInstanceForMyMessage(MessageItem item) {
        return newInstance(item, true);
    }

    private static ManipulateMessageDialogFragment newInstance(MessageItem item, boolean isMine) {
        String title = DateTransformator.getTimeString(item.getLinkTime());

        ManipulateMessageDialogFragment frag = new ManipulateMessageDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putInt(MESSAGE_ID, item.getMessageId());
        args.putInt(MESSAGE_TYPE, item.getContentType());
        args.putString(CURRENT_MESSAGE, item.getContentString());
        args.putBoolean(IS_MINE, isMine);
        if (item.getContentType() == MessageItem.TYPE_COMMENT) {
            args.putInt(FEEDBACK_ID, item.getFeedbackId());
        }
        frag.setArguments(args);
        return frag;
    }

    public static ManipulateMessageDialogFragment newInstanceByTextMessage(ResMessages.TextMessage item, boolean isMine) {
        String title = DateTransformator.getTimeString(item.createTime);

        ManipulateMessageDialogFragment frag = new ManipulateMessageDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putInt(MESSAGE_ID, item.id);
        args.putInt(MESSAGE_TYPE, MessageItem.TYPE_STRING);
        args.putString(CURRENT_MESSAGE, item.content.body);
        args.putBoolean(IS_MINE, isMine);
        frag.setArguments(args);
        return frag;
    }

    public static ManipulateMessageDialogFragment newInstanceByCommentMessage(ResMessages.CommentMessage item, boolean isMine) {
        String title = DateTransformator.getTimeString(item.createTime);

        ManipulateMessageDialogFragment frag = new ManipulateMessageDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putInt(MESSAGE_ID, item.id);
        args.putInt(MESSAGE_TYPE, MessageItem.TYPE_STRING);
        args.putString(CURRENT_MESSAGE, item.content.body);
        args.putBoolean(IS_MINE, isMine);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        // 회면 밖 터치시 다이얼로그 종료
        Dialog me = getDialog();
        me.setCanceledOnTouchOutside(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String title = getArguments().getString(TITLE, "");
        final int messageId = getArguments().getInt(MESSAGE_ID);
        final int messageType = getArguments().getInt(MESSAGE_TYPE);

        final int feedbackId = getArguments().getInt(FEEDBACK_ID, -1);
        final boolean isMine = getArguments().getBoolean(IS_MINE, false);

        final String currentMessage = getArguments().getString(CURRENT_MESSAGE);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_manipulate_message, null);

        final TextView actionDel = (TextView) mainView.findViewById(R.id.txt_action_del_message);
        final TextView actionCopy = (TextView) mainView.findViewById(R.id.txt_action_copy_message);

        if (isMine) {   // 본인이 작성한 메시지가 아닌경우 삭제 메뉴가 활성화되지 않는다.
            actionDel.setVisibility(View.VISIBLE);
        } else {
            actionDel.setVisibility(View.GONE);
        }

        // Delete 메뉴 클릭시.
        actionDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new RequestDeleteMessageEvent(messageType, messageId, feedbackId));
                dismiss();
            }
        });

        // Copy 클릭시.
        actionCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new ConfirmCopyMessageEvent(currentMessage));
                dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mainView)
                .setTitle(title);

        return builder.create();
    }
}
