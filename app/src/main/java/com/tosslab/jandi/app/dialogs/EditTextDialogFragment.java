package com.tosslab.jandi.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.ConfirmCreateEntityEvent;
import com.tosslab.jandi.app.events.ConfirmModifyCdpEvent;
import com.tosslab.jandi.app.events.ConfirmModifyMessageEvent;

import org.androidannotations.annotations.EFragment;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 5. 28..
 * 하나의 EditText 와 확인, 취소 버튼이 존재하는 AlertDialogFragment
 * Entity의 생성, 혹은 수정에 사용된다.
 */
@EFragment
public class EditTextDialogFragment extends DialogFragment {

    public final static int ACTION_CREATE_CDP       = 0;
    public final static int ACTION_MODIFY_CDP       = 1;
    public final static int ACTION_MODIFY_MESSAGE   = 2;

    /**
     * CDP 생성에 사용되는 Dialog.
     * @param actionType
     * @param cdpType
     * @param cdpId
     * @return
     */
    public static EditTextDialogFragment newInstance(int actionType, int cdpType, int cdpId) {
        EditTextDialogFragment frag = new EditTextDialogFragment();
        Bundle args = new Bundle();
        args.putInt("actionType", actionType);
        args.putInt("cdpType", cdpType);
        args.putInt("id", cdpId);
        frag.setArguments(args);
        return frag;
    }

    /**
     * CDP 수정에 사용되는 Dialog.
     * @param actionType
     * @param cdpType
     * @param cdpId
     * @param currentCdpName
     * @return
     */
    public static EditTextDialogFragment newInstance(int actionType, int cdpType
            , int cdpId, String currentCdpName) {
        EditTextDialogFragment frag = new EditTextDialogFragment();
        Bundle args = new Bundle();
        args.putInt("actionType", actionType);
        args.putInt("cdpType", cdpType);
        args.putInt("id", cdpId);
        args.putString("currentMessage", currentCdpName);
        frag.setArguments(args);
        return frag;
    }

    /**
     * 메시지 수정에 사용되는 Dialog
     * @param messageId
     * @param currentMessage
     * @return
     */
    public static EditTextDialogFragment newInstance(int messageType, int messageId, String currentMessage, int feedbackId) {
        EditTextDialogFragment frag = new EditTextDialogFragment();
        Bundle args = new Bundle();
        args.putInt("actionType", ACTION_MODIFY_MESSAGE);
        args.putInt("id", messageId);
        args.putInt("feedbackId", feedbackId);
        args.putString("currentMessage", currentMessage);
        args.putInt("messageType", messageType);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        // 회면 밖 터치시 다이얼로그 종료
        Dialog me = getDialog();
        me.setCanceledOnTouchOutside(true);
        // 키보드 자동 올리기
        me.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        me.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int actionType = getArguments().getInt("actionType");
        final int cdpType = getArguments().getInt("cdpType");
        final int id = getArguments().getInt("id");
        final String currentMessage = getArguments().getString("currentMessage");
        final int feedbackId = getArguments().getInt("feedbackId", -1);
        final int messageType = getArguments().getInt("messageType");

        int titleStringId = obtainTitleByPurpose(actionType, cdpType);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_input_text, null);

        final EditText inputName = (EditText)mainView.findViewById(R.id.et_dialog_input);
        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(inputName, InputMethodManager.SHOW_FORCED);
        // 수정 대화상자의 경우 현재 메시지를 보여준다.
        if (actionType != ACTION_CREATE_CDP) {
            inputName.setText(currentMessage);
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mainView)
                .setTitle(titleStringId)
                .setPositiveButton(R.string.confirm,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                switch (actionType) {
                                    case ACTION_CREATE_CDP:
                                        // CDP 생성의 경우 MainLeftFragment 로 해당 이벤트 전달
                                        EventBus.getDefault().post(new ConfirmCreateEntityEvent(cdpType
                                                , inputName.getText().toString()));
                                        break;
                                    case ACTION_MODIFY_CDP:
                                        // CDP 수정의 경우 MainLeftFragment 로 해당 이벤트 전달
                                        EventBus.getDefault().post(new ConfirmModifyCdpEvent(cdpType
                                                , id
                                                , inputName.getText().toString()));
                                        break;
                                    case ACTION_MODIFY_MESSAGE:
                                    default:
                                        // 메시지 수정의 경우 MainMessageListFragment 로 해당 이벤트 전달
                                        EventBus.getDefault().post(new ConfirmModifyMessageEvent(messageType
                                                , id
                                                , inputName.getText().toString()
                                                , feedbackId));
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
            case JandiConstants.TYPE_CHANNEL:
                return R.string.create_channel;
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                // TODO : Direct Message 에 생성이 필요할까?
                return R.string.create_direct_message;
            case JandiConstants.TYPE_PRIVATE_GROUP:
            default:
                return R.string.create_private_group;
        }
    }

    int obtainTitileForModifyCdp(int cdpType) {
        switch (cdpType) {
            case JandiConstants.TYPE_CHANNEL:
                return R.string.modify_channel;
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                // TODO : Direct Message 에 수정이 필요할까?
                return R.string.modify_private_group;
            case JandiConstants.TYPE_PRIVATE_GROUP:
            default:
                return R.string.modify_direct_message;
        }

    }
}
