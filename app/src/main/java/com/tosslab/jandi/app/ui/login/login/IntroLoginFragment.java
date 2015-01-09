package com.tosslab.jandi.app.ui.login.login;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.profile.ForgotPasswordEvent;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelAccountAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.login.login.model.IntroLoginModel;
import com.tosslab.jandi.app.ui.login.login.viewmodel.IntroLoginViewModel;
import com.tosslab.jandi.app.ui.signup.SignUpActivity_;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OnActivityResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import de.greenrobot.event.EventBus;

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
            JandiPreference.setFirstLogin(getActivity());


            ResAccountInfo accountInfo = JandiAccountDatabaseManager.getInstance(getActivity()).getAccountInfo();
            MixpanelAccountAnalyticsClient mixpanelAccountAnalyticsClient = MixpanelAccountAnalyticsClient.getInstance(getActivity(), accountInfo.getId());
            mixpanelAccountAnalyticsClient.trackAccountSingingIn();


        } else if (httpCode == JandiNetworkException.DATA_NOT_FOUND) {
            introLoginViewModel.loginFail(R.string.err_login_unregistered_id);
        } else {
            introLoginViewModel.loginFail(R.string.err_login_invalid_id_or_password);

        }
    }

    @AfterTextChange(R.id.et_intro_login_email)
    void checkValidEmail(Editable editable) {

        String email = editable.toString();

        if (!TextUtils.equals(email, email.toLowerCase())) {
            introLoginViewModel.setEmailText(email.toLowerCase());
            return;
        }

        introLoginModel.setValidEmail(!FormatConverter.isInvalidEmailString(email));
        introLoginViewModel.setSignInButtonEnable(introLoginModel.isValidEmailPassword());
    }

    @AfterTextChange(R.id.et_intro_login_password)
    void checkValidPassword(Editable editable) {
        introLoginModel.setValidPassword(!TextUtils.isEmpty(editable));
        introLoginViewModel.setSignInButtonEnable(introLoginModel.isValidEmailPassword());
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
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

    @Click(R.id.txt_intro_login_forgot_password)
    void onClickForgotPassword() {
        DialogFragment dialogFragment = EditTextDialogFragment.newInstance(EditTextDialogFragment.ACTION_FORGOT_PASSWORD, introLoginViewModel.getEmailText());
        dialogFragment.show(getFragmentManager(), "dialog");
    }

    public void onEvent(ForgotPasswordEvent event) {
        String email = event.getEmail();
        forgotPassword(email);
    }

    @Background
    void forgotPassword(String email) {
        try {
            introLoginModel.requestPasswordReset(email);
            introLoginViewModel.showSuccessPasswordResetToast();
        } catch (HttpStatusCodeException e) {
            introLoginViewModel.showFailPasswordResetToast();
        }
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