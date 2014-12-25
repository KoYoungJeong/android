package com.tosslab.jandi.app.ui.login.login;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.login.login.model.IntroLoginModel;
import com.tosslab.jandi.app.ui.login.login.viewmodel.IntroLoginViewModel;
import com.tosslab.jandi.app.ui.signup.SignUpActivity_;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OnActivityResult;
import org.springframework.http.HttpStatus;

/**
 * Created by justinygchoi on 14. 11. 13..
 */
@EFragment(R.layout.fragment_intro_input_id)
public class IntroLoginFragment extends Fragment {

    public static final String RES_EXTRA_EMAIL = "res_email";

    private static final int REQ_SIGNUP = 1081;
    @Bean
    public IntroLoginModel introLoginModel;

    @Bean
    public IntroLoginViewModel introLoginViewModel;

    @Background
    void startLogin(String email, String password) {

        int httpCode = introLoginModel.startLogin(email, password);

        introLoginViewModel.dissmissProgressDialog();

        if (httpCode == HttpStatus.OK.value()) {
            introLoginViewModel.loginSuccess(email);
        } else if (httpCode == JandiNetworkException.DATA_NOT_FOUND) {
            introLoginViewModel.loginFail(R.string.err_login_unregistered_id);
        } else {
            introLoginViewModel.loginFail(R.string.err_network);

        }
    }


    /**
     * SignUp
     */
    @Click(R.id.btn_getting_started)
    void onClickSignUp() {

        String emailText = introLoginViewModel.getEmailText();
        SignUpActivity_.intent(IntroLoginFragment.this)
                .email(emailText)
                .startForResult(REQ_SIGNUP);
    }

    /**
     * Login
     */
    @Click(R.id.btn_intro_action_signin_start)
    void onClickLogin() {

        introLoginViewModel.hideKeypad();
        introLoginViewModel.showProgressDialog();

        String emailText = introLoginViewModel.getEmailText();
        String passwordText = introLoginViewModel.getPasswordText();
        startLogin(emailText, passwordText);
    }

    @OnActivityResult(REQ_SIGNUP)
    void activityResultSignUp(int resultCode, Intent dataIntent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        String signedEmail;
        if (dataIntent != null) {
            signedEmail = dataIntent.getStringExtra(RES_EXTRA_EMAIL);
        } else {
            signedEmail = "";
        }
        String emailHost = introLoginModel.getEmailHost(signedEmail);
        introLoginViewModel.showSuccessSignUp(signedEmail, emailHost);
    }
}
