package com.tosslab.jandi.app;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by justinygchoi on 2014. 7. 25..
 */
public class LoginFragmentDialog extends DialogFragment {
    public LoginFragmentDialog() {

    }

    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_login, null);

        final EditText etLoginId = (EditText) mainView.findViewById(R.id.et_login_email);
        final EditText etLoginPasswd = (EditText) mainView.findViewById(R.id.et_login_password);
        Button btnLogin = (Button) mainView.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((IntroActivity)getActivity()).doLogin(etLoginId.getEditableText().toString()
                        , etLoginPasswd.getEditableText().toString());
                dismiss();
            }
        });

        // creating the fullscreen dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(mainView);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        return dialog;
    }
}
