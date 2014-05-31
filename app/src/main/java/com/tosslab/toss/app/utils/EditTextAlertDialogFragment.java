package com.tosslab.toss.app.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.tosslab.toss.app.R;
import com.tosslab.toss.app.events.ChooseNaviActionEvent;
import com.tosslab.toss.app.events.ConfirmCreateCdpEvent;
import com.tosslab.toss.app.events.ConfirmModifyCdpEvent;
import com.tosslab.toss.app.events.ConfirmModifyMessageEvent;

import org.androidannotations.annotations.EFragment;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 5. 28..
 * 하나의 EditText 와 확인, 취소 버튼이 존재하는 AlertDialogFragment
 * Entity의 생성, 혹은 수정에 사용된다.
 */
@EFragment
public class EditTextAlertDialogFragment extends DialogFragment {
    public final static int ACTION_CREATE_CDP       = 0;
    public final static int ACTION_MODIFY_CDP       = 1;
    public final static int ACTION_MODIFY_MESSAGE   = 2;

    /**
     * CDP 생성, CDP 수정에 사용되는 Dialog.
     * @param actionType
     * @param cdpType
     * @param cdpId
     * @return
     */
    public static EditTextAlertDialogFragment newInstance(int actionType, int cdpType, int cdpId) {
        EditTextAlertDialogFragment frag = new EditTextAlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt("actionType", actionType);
        args.putInt("cdpType", cdpType);
        args.putInt("id", cdpId);
        frag.setArguments(args);
        return frag;
    }

    /**
     * 메시지 수정에 사용되는 Dialog
     * @param messageId
     * @param currentMessage
     * @return
     */
    public static EditTextAlertDialogFragment newInstance(int messageId, String currentMessage) {
        EditTextAlertDialogFragment frag = new EditTextAlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt("actionType", ACTION_MODIFY_MESSAGE);
        args.putInt("id", messageId);
        args.putString("currentMessage", currentMessage);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int actionType = getArguments().getInt("actionType");
        final int cdpType = getArguments().getInt("cdpType");
        final int id = getArguments().getInt("id");
        final String currentMessage = getArguments().getString("currentMessage");

        int titleStringId = obtainTitleByPurpose(actionType, cdpType);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_input_text, null);

        final EditText inputName = (EditText)mainView.findViewById(R.id.et_dialog_input);
        // message 수정 대화상자의 경우 현재 메시지를 보여준다.
        if (actionType == ACTION_MODIFY_MESSAGE) {
            inputName.setText(currentMessage);
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mainView)
                .setIcon(android.R.drawable.ic_menu_agenda)
                .setTitle(titleStringId)
                .setPositiveButton(R.string.confirm,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                switch (actionType) {
                                    case ACTION_CREATE_CDP:
                                        // CDP 생성의 경우 NavigationDrawerFragment 로 해당 이벤트 전달
                                        EventBus.getDefault().post(new ConfirmCreateCdpEvent(cdpType
                                                , inputName.getText().toString()));
                                        break;
                                    case ACTION_MODIFY_CDP:
                                        // CDP 수정의 경우 NavigationDrawerFragment 로 해당 이벤트 전달
                                        EventBus.getDefault().post(new ConfirmModifyCdpEvent(cdpType
                                                , id
                                                , inputName.getText().toString()));
                                        break;
                                    case ACTION_MODIFY_MESSAGE:
                                    default:
                                        // 메시지 수정의 경우 MessageListFragment 로 해당 이벤트 전달
                                        EventBus.getDefault().post(new ConfirmModifyMessageEvent(id
                                                , inputName.getText().toString()));
                                        break;

                                }

                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do Nothing
                            }
                        }
                );
        return builder.create();
    }


    /**
     * 본 대화상자를 호출한 목적에 따라 대화 상자의 title 을 달리 함.
     * @param actionType
     * @param cdpType
     * @return
     */
    int obtainTitleByPurpose(int actionType, int cdpType) {
        switch (actionType) {
            case ACTION_CREATE_CDP:
                return obtainTitileForCreateCdp(cdpType);
            case ACTION_MODIFY_CDP:
                return obtainTitileForModifyCdp(cdpType);
            case ACTION_MODIFY_MESSAGE:
            default:
                return R.string.modify_message;
        }
    }

    int obtainTitileForCreateCdp(int cdpType) {
        switch (cdpType) {
            case ChooseNaviActionEvent.TYPE_CHENNEL:
                return R.string.create_channel;
            case ChooseNaviActionEvent.TYPE_DIRECT_MESSAGE:
                // TODO : Direct Message 에 생성이 필요할까?
                return R.string.create_direct_message;
            case ChooseNaviActionEvent.TYPE_PRIVATE_GROUP:
            default:
                return R.string.create_private_group;
        }
    }

    int obtainTitileForModifyCdp(int cdpType) {
        switch (cdpType) {
            case ChooseNaviActionEvent.TYPE_CHENNEL:
                return R.string.modify_channel;
            case ChooseNaviActionEvent.TYPE_DIRECT_MESSAGE:
                // TODO : Direct Message 에 수정이 필요할까?
                return R.string.modify_private_group;
            case ChooseNaviActionEvent.TYPE_PRIVATE_GROUP:
            default:
                return R.string.modify_direct_message;
        }

    }
}
