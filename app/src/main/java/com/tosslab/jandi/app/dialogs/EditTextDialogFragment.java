package com.tosslab.jandi.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.ConfirmCreatePrivateTopicEvent;
import com.tosslab.jandi.app.events.ConfirmCreatePublicTopicEvent;
import com.tosslab.jandi.app.events.ConfirmModifyEntityEvent;
import com.tosslab.jandi.app.events.ConfirmModifyMessageEvent;
import com.tosslab.jandi.app.events.ConfirmModifyProfileEvent;
import com.tosslab.jandi.app.events.ErrorDialogFragmentEvent;

import org.androidannotations.annotations.EFragment;
import org.apache.log4j.Logger;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 5. 28..
 * 하나의 EditText 와 확인, 취소 버튼이 존재하는 AlertDialogFragment
 * Entity의 생성, 혹은 수정에 사용된다.
 */
@EFragment
public class EditTextDialogFragment extends DialogFragment {
    private final Logger log = Logger.getLogger(EditTextDialogFragment.class);

    private final static int MAX_LENGTH_OF_TOPIC_NAME = 60;
    private final static int MAX_LENGTH_OF_PHONE = 20;
    private final static int MAX_LENGTH_OF_STATUS = 60;
    private final static int MAX_LENGTH_OF_DIVISION = 60;
    private final static int MAX_LENGTH_OF_POSITION = 60;

    public final static int ACTION_CREATE_TOPIC = 0;
    public final static int ACTION_MODIFY_TOPIC = 1;
    public final static int ACTION_MODIFY_MESSAGE = 2;
    public final static int ACTION_MODIFY_PROFILE_STATUS = 3;
    public final static int ACTION_MODIFY_PROFILE_PHONE = 4;
    public final static int ACTION_MODIFY_PROFILE_DIVISION = 5;
    public final static int ACTION_MODIFY_PROFILE_POSITION = 6;


    private final static String ARG_ACTION_TYPE = "actionType";
    private final static String ARG_ENTITY_TYPE = "entityType";
    private final static String ARG_ENTITY_ID = "entityId";
    private final static String ARG_CURRENT_MGS = "currentMessage";
    private final static String ARG_FEEDBACK_ID = "feedbackId";
    private final static String ARG_MESSAGE_TYPE = "messageType";

    /**
     * CDP 생성에 사용되는 Dialog.
     *
     * @param actionType
     * @param entityType
     * @param entityId
     * @return
     */
    public static EditTextDialogFragment newInstance(int actionType, int entityType,
                                                     int entityId) {
        EditTextDialogFragment frag = new EditTextDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ACTION_TYPE, actionType);
        args.putInt(ARG_ENTITY_TYPE, entityType);
        args.putInt(ARG_ENTITY_ID, entityId);
        frag.setArguments(args);
        return frag;
    }

    /**
     * CDP 수정에 사용되는 Dialog.
     *
     * @param actionType
     * @param entityType
     * @param entityId
     * @param currentCdpName
     * @return
     */
    public static EditTextDialogFragment newInstance(int actionType, int entityType,
                                                     int entityId, String currentCdpName) {
        EditTextDialogFragment frag = new EditTextDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ACTION_TYPE, actionType);
        args.putInt(ARG_ENTITY_TYPE, entityType);
        args.putInt(ARG_ENTITY_ID, entityId);
        args.putString(ARG_CURRENT_MGS, currentCdpName);
        frag.setArguments(args);
        return frag;
    }

    /**
     * 메시지 수정에 사용되는 Dialog
     *
     * @param messageId
     * @param currentMessage
     * @return
     */
    public static EditTextDialogFragment newInstance(int messageType, int messageId,
                                                     String currentMessage, int feedbackId) {
        EditTextDialogFragment frag = new EditTextDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ACTION_TYPE, ACTION_MODIFY_MESSAGE);
        args.putInt(ARG_ENTITY_ID, messageId);
        args.putInt(ARG_FEEDBACK_ID, feedbackId);
        args.putString(ARG_CURRENT_MGS, currentMessage);
        args.putInt(ARG_MESSAGE_TYPE, messageType);
        frag.setArguments(args);
        return frag;
    }

    /**
     * 프로필 수정 각 항목에 사용되는 Dialog
     *
     * @param actionType
     * @param currentMessage
     * @return
     */
    public static EditTextDialogFragment newInstance(int actionType, String currentMessage) {
        EditTextDialogFragment frag = new EditTextDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ACTION_TYPE, actionType);
        args.putString(ARG_CURRENT_MGS, currentMessage);
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
        me.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        me.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int actionType = getArguments().getInt(ARG_ACTION_TYPE);
        final int entityType = getArguments().getInt(ARG_ENTITY_TYPE);
        final int entityId = getArguments().getInt(ARG_ENTITY_ID);
        final String currentMessage = getArguments().getString(ARG_CURRENT_MGS, "");
        final int feedbackId = getArguments().getInt(ARG_FEEDBACK_ID, -1);
        final int messageType = getArguments().getInt(ARG_MESSAGE_TYPE);

        int titleStringId = obtainTitleByPurpose(actionType, entityType);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_input_text, null);

        // 입력 상자 타입에 따른 설정
        final EditText inputName = (EditText) mainView.findViewById(R.id.et_dialog_input);
        switch (actionType) {
            case ACTION_MODIFY_PROFILE_PHONE:
                inputName.setInputType(InputType.TYPE_CLASS_PHONE);
                inputName.setHint(R.string.jandi_profile_phone_number_hint);
                break;
            case ACTION_MODIFY_PROFILE_DIVISION:
            case ACTION_MODIFY_PROFILE_POSITION:
                inputName.setHint(R.string.jandi_profile_division_hint);
                break;
            default:
                break;
        }

        // 수정 대화상자의 경우 현재 메시지를 보여준다.
        inputName.setText(currentMessage);

        InputMethodManager imm
                = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(inputName, InputMethodManager.SHOW_FORCED);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mainView)
                .setTitle(titleStringId)
                .setPositiveButton(R.string.jandi_confirm,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String input = inputName.getText().toString();
                                log.debug("length of input is " + input.length());
                                switch (actionType) {
                                    case ACTION_CREATE_TOPIC:
                                        if (input.length() > MAX_LENGTH_OF_TOPIC_NAME) {
                                            EventBus.getDefault().post(
                                                    new ErrorDialogFragmentEvent(R.string.err_profile_exceed_length_of_topic_name)
                                            );
                                            return;
                                        }

                                        // Topic 혹은 Group 생성일 경우 해당 이벤트 전달
                                        if (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) {
                                            EventBus.getDefault().post(
                                                    new ConfirmCreatePublicTopicEvent(input)
                                            );
                                        } else {
                                            EventBus.getDefault().post(
                                                    new ConfirmCreatePrivateTopicEvent(input)
                                            );
                                        }
                                        break;
                                    case ACTION_MODIFY_TOPIC:
                                        if (input.length() > MAX_LENGTH_OF_TOPIC_NAME) {
                                            EventBus.getDefault().post(
                                                    new ErrorDialogFragmentEvent(R.string.err_profile_exceed_length_of_topic_name)
                                            );
                                            return;
                                        }
                                        // CDP 수정의 경우 MainLeftFragment 로 해당 이벤트 전달
                                        EventBus.getDefault().post(
                                                new ConfirmModifyEntityEvent(entityType, entityId, input)
                                        );
                                        break;
                                    case ACTION_MODIFY_MESSAGE:
                                        // 메시지 수정의 경우 MainMessageListFragment 로 해당 이벤트 전달
                                        EventBus.getDefault().post(
                                                new ConfirmModifyMessageEvent(
                                                        messageType,
                                                        entityId,
                                                        input,
                                                        feedbackId)
                                        );
                                        break;
                                    case ACTION_MODIFY_PROFILE_STATUS:
                                        if (input.length() > MAX_LENGTH_OF_STATUS) {
                                            EventBus.getDefault().post(
                                                    new ErrorDialogFragmentEvent(R.string.err_profile_exceed_length_of_status_message)
                                            );
                                            return;
                                        }
                                    case ACTION_MODIFY_PROFILE_PHONE:
                                        if (input.length() > MAX_LENGTH_OF_PHONE) {
                                            EventBus.getDefault().post(
                                                    new ErrorDialogFragmentEvent(R.string.err_profile_exceed_length_of_phone)
                                            );
                                            return;
                                        }
                                    case ACTION_MODIFY_PROFILE_DIVISION:
                                        if (input.length() > MAX_LENGTH_OF_DIVISION) {
                                            EventBus.getDefault().post(
                                                    new ErrorDialogFragmentEvent(R.string.err_profile_exceed_length_of_title)
                                            );
                                            return;
                                        }
                                    case ACTION_MODIFY_PROFILE_POSITION:
                                        if (input.length() > MAX_LENGTH_OF_POSITION) {
                                            EventBus.getDefault().post(
                                                    new ErrorDialogFragmentEvent(R.string.err_profile_exceed_length_of_title)
                                            );
                                            return;
                                        }
                                        EventBus.getDefault().post(new ConfirmModifyProfileEvent(actionType, input));
                                        break;
                                    default:
                                        // DO NOTHING
                                        break;
                                }

                            }
                        }
                )
                .setNegativeButton(R.string.jandi_cancel,
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
     *
     * @param actionType
     * @param entityType
     * @return
     */
    int obtainTitleByPurpose(int actionType, int entityType) {
        switch (actionType) {
            case ACTION_CREATE_TOPIC:
                return obtainTitileForCreateCdp(entityType);
            case ACTION_MODIFY_TOPIC:
                return obtainTitileForModifyCdp(entityType);
            case ACTION_MODIFY_MESSAGE:
                return R.string.modify_message;
            case ACTION_MODIFY_PROFILE_STATUS:
                return R.string.jandi_profile_status_message;
            case ACTION_MODIFY_PROFILE_PHONE:
                return R.string.jandi_profile_phone_number;
            case ACTION_MODIFY_PROFILE_DIVISION:
                return R.string.jandi_profile_division;
            case ACTION_MODIFY_PROFILE_POSITION:
                return R.string.jandi_profile_position;
            default:
                return R.string.jandi_empty;


        }
    }

    int obtainTitileForCreateCdp(int entityType) {
        switch (entityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return R.string.jandi_create_channel;
            case JandiConstants.TYPE_PRIVATE_TOPIC:
            default:
                return R.string.jandi_create_privategroup;
        }
    }

    int obtainTitileForModifyCdp(int entityType) {
        switch (entityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return R.string.jandi_modify_channel;
            case JandiConstants.TYPE_PRIVATE_TOPIC:
            default:
                return R.string.jandi_modify_private_group;
        }

    }
}
