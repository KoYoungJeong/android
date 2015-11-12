package com.tosslab.jandi.app.ui.login.login;

import android.app.DialogFragment;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.profile.ForgotPasswordEvent;
import com.tosslab.jandi.app.network.exception.ExceptionData;
import com.tosslab.jandi.app.network.mixpanel.MixpanelAccountAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.login.IntroMainActivity;
import com.tosslab.jandi.app.ui.login.login.model.IntroLoginModel;
import com.tosslab.jandi.app.ui.login.login.viewmodel.IntroLoginViewModel;
import com.tosslab.jandi.app.ui.signup.account.SignUpActivity_;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.SignOutUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Created by justinygchoi on 14. 11. 13..
 */
@EFragment(R.layout.fragment_intro_input_id)
public class IntroLoginFragment extends Fragment implements IntroMainActivity.KeyboardHandler {

    private static final int REQ_SIGNUP = 1081;

    @Bean
    public IntroLoginModel introLoginModel;

    @Bean
    public IntroLoginViewModel introLoginViewModel;

    @AfterViews
    void init() {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.ScreenView)
                        .property(PropertyKey.ScreenView, ScreenViewProperty.LOGIN_PAGE)
                        .build());

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.SignIn);
    }

    @Background
    void startLogin(String email, String password) {

        SignOutUtil.initSignData();

        ResAccessToken accessToken = null;
        try {
            accessToken = introLoginModel.login(email, password);
            introLoginModel.saveTokenInfo(accessToken);

        } catch (RetrofitError error) {

            introLoginViewModel.dissmissProgressDialog();
            if (error.getKind() == RetrofitError.Kind.HTTP) {
                try {
                    ExceptionData exceptionData = (ExceptionData) error.getBodyAs(ExceptionData.class);
                    switch (exceptionData.getCode()) {
                        case 40000:
                            introLoginViewModel.loginFail(R.string.err_login_invalid_id_or_password);
                            break;
                        case 40007:
                            introLoginViewModel.loginFail(R.string.err_login_unregistered_id);
                            break;
                    }
                    introLoginModel.trackSignInFail(JandiConstants.NetworkError.DATA_NOT_FOUND);
                } catch (Exception e) {
                    introLoginViewModel.loginFail(R.string.err_network);
                    introLoginModel.trackSignInFail(JandiConstants.NetworkError.BAD_REQUEST);
                }
            } else {
                introLoginViewModel.loginFail(R.string.err_network);
                introLoginModel.trackSignInFail(JandiConstants.NetworkError.BAD_REQUEST);
            }

        }

        if (accessToken != null) {
            try {
                ResAccountInfo accountInfo = introLoginModel.getAccountInfo();
                introLoginModel.saveAccountInfo(accountInfo);

                introLoginViewModel.loginSuccess(email);
                JandiPreference.setFirstLogin(getActivity());

                MixpanelAccountAnalyticsClient
                        .getInstance(getActivity(), accountInfo.getId())
                        .trackAccountSingingIn();

                introLoginModel.trackSignInSuccess();
            } catch (Exception e) {
                e.printStackTrace();
                introLoginViewModel.loginFail(R.string.err_network);
            }

        }
        introLoginViewModel.dissmissProgressDialog();

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
                .start();
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SignIn, AnalyticsValue.Action.SignUp);

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
        if (NetworkCheckUtil.isConnected()) {
            startLogin(emailText, passwordText);
        } else {
            introLoginViewModel.loginFail(R.string.err_network);
        }

    }

    @Click(R.id.txt_intro_login_forgot_password)
    void onClickForgotPassword() {
        DialogFragment dialogFragment =
                EditTextDialogFragment.newInstance(
                        EditTextDialogFragment.ACTION_FORGOT_PASSWORD, introLoginViewModel.getEmailText());
        dialogFragment.show(getFragmentManager(), "dialog");
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SignIn, AnalyticsValue.Action.ForgotPW);
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
        } catch (Exception e) {
            introLoginViewModel.showFailPasswordResetToast();
        }
    }

    @Override
    public void hideKeyboard() {
        if (introLoginViewModel == null) {
            return;
        }

        introLoginViewModel.hideKeypad();
    }
}
