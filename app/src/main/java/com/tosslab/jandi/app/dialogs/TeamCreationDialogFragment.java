package com.tosslab.jandi.app.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestTeamCreationEvent;
import com.tosslab.jandi.app.utils.FormatConverter;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 14. 11. 13..
 */
public class TeamCreationDialogFragment extends DialogFragment {
    private final static String ARG_EMAIL    = "email";

    public static TeamCreationDialogFragment newInstance(String email) {
        TeamCreationDialogFragment frag = new TeamCreationDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
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

    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final String email = getArguments().getString(ARG_EMAIL, "");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_fragment_input_text, null);

        final Button buttonTeamCreate
                = (Button) mainView.findViewById(R.id.btn_dialog_input_confirm);
        final EditText editTextId = (EditText) mainView.findViewById(R.id.et_dialog_input_text);
        if (email.length() > 0) {
            if (FormatConverter.isInvalidEmailString(email) == false) {
                buttonTeamCreate.setSelected(true);
            }
            editTextId.setText(email);
        }

        editTextId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if (FormatConverter.isInvalidEmailString(editable.toString())) {
                    buttonTeamCreate.setSelected(false);
                } else {
                    buttonTeamCreate.setSelected(true);
                }
            }
        });
        buttonTeamCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonTeamCreate.isSelected()) {
                    String myEmailId = editTextId.getText().toString();
                    EventBus.getDefault().post(new RequestTeamCreationEvent(myEmailId));
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
}
