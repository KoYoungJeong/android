package com.tosslab.jandi.app.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.ConfirmModifyProfileEvent;
import com.tosslab.jandi.app.events.entities.ConfirmCreatePrivateTopicEvent;
import com.tosslab.jandi.app.events.entities.ConfirmCreatePublicTopicEvent;
import com.tosslab.jandi.app.events.entities.ConfirmModifyTopicEvent;
import com.tosslab.jandi.app.events.profile.ForgotPasswordEvent;
import com.tosslab.jandi.app.events.profile.NewEmailEvent;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 5. 28..
 * 하나의 EditText 와 확인, 취소 버튼이 존재하는 AlertDialogFragment
 * Entity의 생성, 혹은 수정에 사용된다.
 */
public class EditTextDialogFragment extends DialogFragment {
    public final static int ACTION_CREATE_TOPIC = 0;
    public final static int ACTION_MODIFY_TOPIC = 1;
    public final static int ACTION_MODIFY_PROFILE_STATUS = 3;
    public final static int ACTION_MODIFY_PROFILE_PHONE = 4;
    public final static int ACTION_MODIFY_PROFILE_DIVISION = 5;
    public final static int ACTION_MODIFY_PROFILE_POSITION = 6;
    public final static int ACTION_MODIFY_PROFILE_ACCOUNT_NAME = 7;
    public final static int ACTION_FORGOT_PASSWORD = 8;
    public final static int ACTION_NEW_EMAIL = 9;
    public static final int ACTION_MODIFY_PROFILE_MEMBER_NAME = 10;
    private final static int MAX_LENGTH_OF_TOPIC_NAME = 60;
    private final static int MAX_LENGTH_OF_PHONE = 20;
    private static final int MAX_LENGTH_OF_ACCOUNT_NAME = 30;
    private final static int MAX_LENGTH_OF_STATUS = 60;
    private final static int MAX_LENGTH_OF_DIVISION = 60;
    private final static int MAX_LENGTH_OF_POSITION = 60;
    private final static String ARG_ACTION_TYPE = "actionType";
    private final static String ARG_TOPIC_TYPE = "topicType";
    private final static String ARG_TOPIC_ID = "topicId";
    private final static String ARG_CURRENT_MGS = "currentMessage";

    private int actionType;
    private int topicType;
    private long topicId;
    private String currentMessage;

    /**
     * topic 생성에 사용되는 Dialog.
     *
     * @param actionType
     * @param topicType
     * @param topicId
     * @return
     */
    public static EditTextDialogFragment newInstance(int actionType, int topicType, long topicId) {
        EditTextDialogFragment frag = new EditTextDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ACTION_TYPE, actionType);
        args.putInt(ARG_TOPIC_TYPE, topicType);
        args.putLong(ARG_TOPIC_ID, topicId);
        frag.setArguments(args);
        return frag;
    }

    /**
     * topic 수정에 사용되는 Dialog.
     *
     * @param actionType
     * @param topicType
     * @param topicId
     * @param currentCdpName
     * @return
     */
    public static EditTextDialogFragment newInstance(int actionType, int topicType,
                                                     long topicId, String currentCdpName) {
        EditTextDialogFragment frag = new EditTextDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ACTION_TYPE, actionType);
        args.putInt(ARG_TOPIC_TYPE, topicType);
        args.putLong(ARG_TOPIC_ID, topicId);
        args.putString(ARG_CURRENT_MGS, currentCdpName);
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
        actionType = getArguments().getInt(ARG_ACTION_TYPE);
        topicType = getArguments().getInt(ARG_TOPIC_TYPE);
        topicId = getArguments().getLong(ARG_TOPIC_ID);
        currentMessage = getArguments().getString(ARG_CURRENT_MGS, "");

        int titleStringId = obtainTitleByPurpose(actionType, topicType);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_fragment_input_text, null);

        TextView tvTitle = (TextView) mainView.findViewById(R.id.tv_popup_title);
        tvTitle.setText(titleStringId);

        final EditText editTextInput = (EditText) mainView.findViewById(R.id.et_dialog_input_text);
        editTextInput.setText(currentMessage);
        editTextInput.setSelection(currentMessage.length());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300);
        // creating the fullscreen dialog
        builder.setCancelable(true)
                .setView(mainView)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
                    String input = editTextInput.getText().toString();
                    LogUtil.d("length of input is " + input.length());

                    switch (actionType) {
                        case ACTION_CREATE_TOPIC:
                            if (topicType == JandiConstants.TYPE_PUBLIC_TOPIC) {
                                EventBus.getDefault().post(new ConfirmCreatePublicTopicEvent(input));
                            } else {
                                EventBus.getDefault().post(new ConfirmCreatePrivateTopicEvent(input));
                            }
                            break;
                        case ACTION_MODIFY_TOPIC:
                            EventBus.getDefault().post(
                                    new ConfirmModifyTopicEvent(topicType, topicId, input)
                            );
                            break;
                        case ACTION_MODIFY_PROFILE_STATUS:
                        case ACTION_MODIFY_PROFILE_PHONE:
                        case ACTION_MODIFY_PROFILE_DIVISION:
                        case ACTION_MODIFY_PROFILE_POSITION:
                        case ACTION_MODIFY_PROFILE_ACCOUNT_NAME:
                        case ACTION_MODIFY_PROFILE_MEMBER_NAME:
                            EventBus.getDefault().post(new ConfirmModifyProfileEvent(actionType, input));
                            break;
                        case ACTION_FORGOT_PASSWORD:
                            EventBus.getDefault().post(new ForgotPasswordEvent(input));
                            break;
                        case ACTION_NEW_EMAIL:
                            EventBus.getDefault().post(new NewEmailEvent(input));
                            break;
                        default:
                            // DO NOTHING
                            break;
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setOnShowListener(dialog1 -> setupPositiveButton(dialog, currentMessage,
                actionType, currentMessage));
        setEditTextByPurpose(dialog, editTextInput, currentMessage, actionType);

        return dialog;

    }

    private void setEditTextByPurpose(AlertDialog dialog, final EditText input,
                                      final String currentMessage, final int purpose) {
        switch (purpose) {
            case ACTION_MODIFY_PROFILE_PHONE:
                input.setInputType(InputType.TYPE_CLASS_PHONE);
                input.setHint(R.string.jandi_enter_phone_number);
                break;
            case ACTION_MODIFY_PROFILE_DIVISION:
                input.setHint(R.string.jandi_profile_division_hint);
                break;
            case ACTION_MODIFY_PROFILE_POSITION:
                input.setHint(R.string.jandi_profile_position_hint);
                break;
            case ACTION_MODIFY_PROFILE_ACCOUNT_NAME:
            case ACTION_MODIFY_PROFILE_MEMBER_NAME:
                input.setHint(R.string.jandi_enter_name);
                break;
            case ACTION_FORGOT_PASSWORD:
            case ACTION_NEW_EMAIL:
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                input.setHint(R.string.jandi_user_id);
                Button btnConfirm = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnConfirm.setEnabled(!FormatConverter.isInvalidEmailString(input.getText().toString()));
                setConfirmColor(btnConfirm);
                break;
            default:
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("");
                break;
        }

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setupPositiveButton(dialog, editable.toString(), purpose, currentMessage);
            }
        });

        int maxLength = getEditTextMaxLength(purpose);

        if (maxLength > 0) {
            input.setMaxEms(maxLength);
        }

    }

    private int getEditTextMaxLength(int purpose) {

        switch (purpose) {
            case ACTION_CREATE_TOPIC:
            case ACTION_MODIFY_TOPIC:
                return MAX_LENGTH_OF_TOPIC_NAME;
            case ACTION_MODIFY_PROFILE_STATUS:
                return MAX_LENGTH_OF_STATUS;
            case ACTION_MODIFY_PROFILE_PHONE:
                return MAX_LENGTH_OF_PHONE;
            case ACTION_MODIFY_PROFILE_DIVISION:
                return MAX_LENGTH_OF_DIVISION;
            case ACTION_MODIFY_PROFILE_POSITION:
                return MAX_LENGTH_OF_POSITION;
            case ACTION_MODIFY_PROFILE_ACCOUNT_NAME:
            case ACTION_MODIFY_PROFILE_MEMBER_NAME:
                return MAX_LENGTH_OF_ACCOUNT_NAME;
            default:
                return -1;
        }
    }

    private void setupPositiveButton(AlertDialog dialog, String editable, int purpose, String currentMessage) {
        Button confirm = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        if (confirm == null) {
            return;
        }

        int inputLength = editable.length();

        switch (purpose) {
            case ACTION_CREATE_TOPIC:
            case ACTION_MODIFY_TOPIC:
                confirm.setEnabled((inputLength > 0)
                        && TextUtils.getTrimmedLength(editable) > 0
                        && (inputLength < MAX_LENGTH_OF_TOPIC_NAME)
                        && !editable.equals(currentMessage));
                break;
            case ACTION_MODIFY_PROFILE_STATUS:
                confirm.setEnabled(inputLength < MAX_LENGTH_OF_STATUS && !editable.equals(currentMessage));
                break;
            case ACTION_MODIFY_PROFILE_PHONE:
                confirm.setEnabled(inputLength < MAX_LENGTH_OF_PHONE && !editable.equals(currentMessage));
                break;
            case ACTION_MODIFY_PROFILE_DIVISION:
                confirm.setEnabled(inputLength < MAX_LENGTH_OF_DIVISION && !editable.equals(currentMessage));
                break;
            case ACTION_MODIFY_PROFILE_POSITION:
                confirm.setEnabled(inputLength < MAX_LENGTH_OF_POSITION && !editable.equals(currentMessage));
                break;
            case ACTION_MODIFY_PROFILE_ACCOUNT_NAME:
            case ACTION_MODIFY_PROFILE_MEMBER_NAME:
                boolean isEnable = inputLength > 0
                        && inputLength <= MAX_LENGTH_OF_ACCOUNT_NAME
                        && !editable.equals(currentMessage);
                confirm.setEnabled(isEnable);
                break;
            case ACTION_FORGOT_PASSWORD:
            case ACTION_NEW_EMAIL:
                confirm.setEnabled(!FormatConverter.isInvalidEmailString(editable));
                break;
            default:
                // DO NOTHING
                break;
        }

        setConfirmColor(confirm);

    }

    private void setConfirmColor(Button confirm) {
        if (confirm.isEnabled()) {
            confirm.setTextColor(JandiApplication.getContext()
                    .getResources().getColor(R.color.button_text_color));
        } else {
            confirm.setTextColor(JandiApplication.getContext()
                    .getResources().getColor(R.color.button_text_color_dim));
        }
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
            case ACTION_MODIFY_PROFILE_STATUS:
                return R.string.jandi_profile_status_message;
            case ACTION_MODIFY_PROFILE_PHONE:
                return R.string.jandi_profile_phone_number;
            case ACTION_MODIFY_PROFILE_DIVISION:
                return R.string.jandi_profile_division;
            case ACTION_MODIFY_PROFILE_POSITION:
                return R.string.jandi_profile_position;
            case ACTION_MODIFY_PROFILE_ACCOUNT_NAME:
            case ACTION_MODIFY_PROFILE_MEMBER_NAME:
                return R.string.jandi_title_name;
            case ACTION_FORGOT_PASSWORD:
                return R.string.password_forgot_email_type_info;
            case ACTION_NEW_EMAIL:
                return R.string.jandi_user_id;
            default:
                return R.string.jandi_default_space;
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
