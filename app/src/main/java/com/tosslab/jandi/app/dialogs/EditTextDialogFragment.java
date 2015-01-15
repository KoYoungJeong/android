package com.tosslab.jandi.app.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.ConfirmModifyProfileEvent;
import com.tosslab.jandi.app.events.entities.ConfirmCreatePrivateTopicEvent;
import com.tosslab.jandi.app.events.entities.ConfirmCreatePublicTopicEvent;
import com.tosslab.jandi.app.events.entities.ConfirmModifyTopicEvent;
import com.tosslab.jandi.app.events.profile.ForgotPasswordEvent;
import com.tosslab.jandi.app.events.profile.NewEmailEvent;
import com.tosslab.jandi.app.utils.FormatConverter;

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
    private static final int MAX_LENGTH_OF_ACCOUNT_NAME = 20;
    private final static int MAX_LENGTH_OF_STATUS = 60;
    private final static int MAX_LENGTH_OF_DIVISION = 60;
    private final static int MAX_LENGTH_OF_POSITION = 60;
    private final static String ARG_ACTION_TYPE = "actionType";
    private final static String ARG_TOPIC_TYPE = "topicType";
    private final static String ARG_TOPIC_ID = "topicId";
    private final static String ARG_CURRENT_MGS = "currentMessage";
    private final Logger log = Logger.getLogger(EditTextDialogFragment.class);

    /**
     * topic 생성에 사용되는 Dialog.
     *
     * @param actionType
     * @param topicType
     * @param topicId
     * @return
     */
    public static EditTextDialogFragment newInstance(int actionType, int topicType, int topicId) {
        EditTextDialogFragment frag = new EditTextDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ACTION_TYPE, actionType);
        args.putInt(ARG_TOPIC_TYPE, topicType);
        args.putInt(ARG_TOPIC_ID, topicId);
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
                                                     int topicId, String currentCdpName) {
        EditTextDialogFragment frag = new EditTextDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ACTION_TYPE, actionType);
        args.putInt(ARG_TOPIC_TYPE, topicType);
        args.putInt(ARG_TOPIC_ID, topicId);
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
        final int actionType = getArguments().getInt(ARG_ACTION_TYPE);
        final int topicType = getArguments().getInt(ARG_TOPIC_TYPE);
        final int topicId = getArguments().getInt(ARG_TOPIC_ID);
        final String currentMessage = getArguments().getString(ARG_CURRENT_MGS, "");

        int titleStringId = obtainTitleByPurpose(actionType, topicType);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_fragment_input_text, null);


        final EditText editTextInput = (EditText) mainView.findViewById(R.id.et_dialog_input_text);
        final Button buttonConfirm
                = (Button) mainView.findViewById(R.id.btn_dialog_input_confirm);
        final TextView title = (TextView) mainView.findViewById(R.id.txt_dialog_input_text);
        // 제목 설정
        title.setText(titleStringId);
        // 입력 상자 타입에 따른 설정
        setEditTextByPurpose(editTextInput, buttonConfirm, currentMessage, actionType);
        // 수정 대화상자의 경우 현재 메시지를 보여준다.
        editTextInput.setText(currentMessage);
        editTextInput.setSelection(currentMessage.length());

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonConfirm.isSelected()) {
                    String input = editTextInput.getText().toString();
                    log.debug("length of input is " + input.length());

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
                    dismiss();
                }
            }
        });

        // creating the fullscreen dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(mainView);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return dialog;

    }

    private void setEditTextByPurpose(final EditText input, final Button confirm,
                                      final String currentMessage, final int purpose) {
        confirm.setText(R.string.jandi_confirm);

        switch (purpose) {
            case ACTION_MODIFY_PROFILE_PHONE:
                input.setInputType(InputType.TYPE_CLASS_PHONE);
                input.setHint(R.string.jandi_profile_phone_number_hint);
                break;
            case ACTION_MODIFY_PROFILE_DIVISION:
                input.setHint(R.string.jandi_profile_division_hint);
                break;
            case ACTION_MODIFY_PROFILE_POSITION:
                input.setHint(R.string.jandi_profile_division_hint);
                break;
            case ACTION_MODIFY_PROFILE_ACCOUNT_NAME:
            case ACTION_MODIFY_PROFILE_MEMBER_NAME:
                input.setHint(R.string.jandi_title_name);
                break;
            case ACTION_FORGOT_PASSWORD:
            case ACTION_NEW_EMAIL:
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                input.setHint(R.string.jandi_user_id);
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
                String inputText = editable.toString();
                int inputLength = inputText.length();


                switch (purpose) {
                    case ACTION_CREATE_TOPIC:
                    case ACTION_MODIFY_TOPIC:
                        confirm.setSelected((inputLength > 0)
                                && (inputLength < MAX_LENGTH_OF_TOPIC_NAME)
                                && (!inputText.equals(currentMessage)));
                        break;
                    case ACTION_MODIFY_PROFILE_STATUS:
                        confirm.setSelected(inputLength < MAX_LENGTH_OF_STATUS);
                        break;
                    case ACTION_MODIFY_PROFILE_PHONE:
                        confirm.setSelected(inputLength < MAX_LENGTH_OF_PHONE);
                        break;
                    case ACTION_MODIFY_PROFILE_DIVISION:
                        confirm.setSelected(inputLength < MAX_LENGTH_OF_DIVISION);
                        break;
                    case ACTION_MODIFY_PROFILE_POSITION:
                        confirm.setSelected(inputLength < MAX_LENGTH_OF_POSITION);
                        break;
                    case ACTION_MODIFY_PROFILE_ACCOUNT_NAME:
                    case ACTION_MODIFY_PROFILE_MEMBER_NAME:
                        confirm.setSelected(inputLength < MAX_LENGTH_OF_ACCOUNT_NAME);
                        break;
                    case ACTION_FORGOT_PASSWORD:
                    case ACTION_NEW_EMAIL:
                        confirm.setSelected(!FormatConverter.isInvalidEmailString(inputText));
                        break;
                    default:
                        // DO NOTHING
                        break;
                }
            }
        });

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
            case ACTION_NEW_EMAIL:
                return R.string.jandi_user_id;
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
