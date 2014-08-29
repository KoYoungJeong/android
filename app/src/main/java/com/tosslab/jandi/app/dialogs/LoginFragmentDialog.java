package com.tosslab.jandi.app.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.LoginActivity;
import com.tosslab.jandi.app.utils.JandiPreference;

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
        String savedId = JandiPreference.getMyId(getActivity());
        if (!savedId.isEmpty()) {
            etLoginId.setText(savedId);
        }
        Button btnLogin = (Button) mainView.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etLoginPasswd.getWindowToken(), 0);
                ((LoginActivity)getActivity()).pressLoginButton(etLoginId.getEditableText().toString()
                        , etLoginPasswd.getEditableText().toString());
            }
        });

        // creating the fullscreen dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(mainView);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return dialog;
    }
}
