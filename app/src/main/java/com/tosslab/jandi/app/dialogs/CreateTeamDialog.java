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

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 10. 13..
 */
public class CreateTeamDialog extends DialogFragment {
    private final static String ARG_EMAIL    = "email";

    public static CreateTeamDialog newInstance(String email) {
        CreateTeamDialog frag = new CreateTeamDialog();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        Dialog me = getDialog();
        // 키보드 자동 올리기
        me.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        me.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final String email = getArguments().getString(ARG_EMAIL, "");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_team_creation, null);

        final Button buttonTeamCreate = (Button) mainView.findViewById(R.id.btn_team_creation);
        final EditText editTextId = (EditText) mainView.findViewById(R.id.et_team_creation_id);
        if (email.length() > 0) {
            editTextId.setText(email);
        }

        editTextId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().length() > 0) {
                    buttonTeamCreate.setBackgroundResource(R.drawable.btn_send_selector);
                } else {
                    buttonTeamCreate.setBackgroundResource(R.color.jandi_inactive_button);
                }
            }
        });
        buttonTeamCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myEmailId = editTextId.getText().toString();
                EventBus.getDefault().post(new RequestTeamCreationEvent(myEmailId));
                dismiss();
            }
        });

        // creating the fullscreen dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(mainView);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return dialog;
    }
}
