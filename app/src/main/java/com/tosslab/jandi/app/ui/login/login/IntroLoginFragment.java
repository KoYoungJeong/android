package com.tosslab.jandi.app.ui.login.login;

import android.app.Fragment;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.login.login.model.IntroLoginModel;
import com.tosslab.jandi.app.ui.login.login.viewmodel.IntroLoginViewModel;
import com.tosslab.jandi.app.ui.signup.SignUpActivity_;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.springframework.http.HttpStatus;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 14. 11. 13..
 */
@EFragment(R.layout.fragment_intro_input_id)
public class IntroLoginFragment extends Fragment {

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
        SignUpActivity_.intent(getActivity())
                .start();
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
}
