package com.tosslab.jandi.app.ui.signup.account;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.mixpanel.MixpanelAccountAnalyticsClient;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.signup.account.model.SignUpModel;
import com.tosslab.jandi.app.ui.signup.account.to.CheckPointsHolder;
import com.tosslab.jandi.app.ui.term.TermActivity;
import com.tosslab.jandi.app.ui.term.TermActivity_;
import com.tosslab.jandi.app.utils.LanguageUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;

import retrofit.RetrofitError;
import retrofit.mime.TypedByteArray;

/**
 * Created by justinygchoi on 14. 12. 11..
 */
@EActivity(R.layout.activity_signup)
public class SignUpActivity extends BaseAppCompatActivity {

    @Bean
    SignUpViewModel signUpViewModel;

    @Bean
    SignUpModel signUpModel;

    @Extra
    String email;

    @AfterViews
    void init() {
        setUpActionBar();
        signUpViewModel.setDefaultEmail(email);

        MixpanelAccountAnalyticsClient.getInstance(SignUpActivity.this, null)
                .pageViewAccountCreate();

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.SignUp);
    }

    private void setUpActionBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        // Set up the action bar.
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    @OptionsItem(android.R.id.home)
    void homeSelected() {
        finish();
    }

    @AfterTextChange(R.id.et_signup_email)
    void changeValidEmail(Editable text) {
        String email = text.toString();

        if (!TextUtils.equals(email, email.toLowerCase())) {
            signUpViewModel.setEmailText(email.toLowerCase());
            return;
        }

        boolean isValidEmail = signUpModel.isValidEmail(email);

        int textColorRes = signUpModel.getEmailTextColor(isValidEmail);
        int validState = signUpModel.getEmailValidValue(isValidEmail);

        signUpModel.activateSignUpButtonByEmail(validState);
        boolean allValid = signUpModel.isAllValid();
        signUpViewModel.activateSignUpButton(allValid);
        signUpViewModel.editTextSignUpEmail.setTextColor(textColorRes);
        signUpViewModel.toggleEmailAlert(isValidEmail);
    }

    @AfterTextChange(R.id.et_signup_password)
    void changePasswordStrength(Editable text) {
        int strength = signUpModel.checkPasswordStrength(text.toString());
        signUpViewModel.setStrengthBarometer(strength);

        int textColorRes = signUpModel.getSignUpPasswordTextColor(strength);
        int validState = signUpModel.getSignUpButtonState(strength);

        signUpModel.activateSignUpButtonByPassword(validState);
        boolean allValid = signUpModel.isAllValid();
        signUpViewModel.activateSignUpButton(allValid);
        signUpViewModel.editTextSignUpPassword.setTextColor(textColorRes);
        signUpViewModel.togglePasswordAlert(validState);
    }

    @AfterTextChange(R.id.et_signup_name)
    void changeName(Editable text) {
        int valid = signUpModel.getNameValidState(text.length());
        signUpModel.activateSignUpButtonByName(valid);
        boolean allValid = signUpModel.isAllValid();
        signUpViewModel.activateSignUpButton(allValid);
        signUpViewModel.toggleNameAlert(valid);
    }

    @Click(R.id.txt_signup_agree_tos)
    void clickAgreeEulaLink() {
        TermActivity_.intent(SignUpActivity.this)
                .termMode(TermActivity.Mode.Agreement.name())
                .start();
    }

    @Click(R.id.txt_signup_agree_pp)
    void clickAgreePrivacyLink() {
        TermActivity_.intent(SignUpActivity.this)
                .termMode(TermActivity.Mode.Privacy.name())
                .start();
    }

    @Click({R.id.btn_signup_agree_tos, R.id.ly_signup_agree_tos})
    void clickAgreeEula() {
        signUpViewModel.toggleEula();
        boolean isAllAgreed = signUpViewModel.checkAllAgree();
        signUpModel.activateSignUpButtonByAgreeAll(isAllAgreed ? CheckPointsHolder.VALID : CheckPointsHolder.INVALID);

        boolean allValid = signUpModel.isAllValid();
        signUpViewModel.activateSignUpButton(allValid);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SignUp, AnalyticsValue.Action.AgreeTermsofService);
    }

    @Click({R.id.btn_signup_agree_pp, R.id.ly_signup_agree_pp})
    void clickAgreePrivate() {
        signUpViewModel.togglePrivate();
        boolean isAllAgreed = signUpViewModel.checkAllAgree();
        signUpModel.activateSignUpButtonByAgreeAll(isAllAgreed ? CheckPointsHolder.VALID : CheckPointsHolder.INVALID);

        boolean allValid = signUpModel.isAllValid();
        signUpViewModel.activateSignUpButton(allValid);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SignUp, AnalyticsValue.Action.AgreePrivacyPolicy);
    }

    @Click({R.id.btn_signup_agree_all, R.id.ly_signup_agree_all})
    void clickAgreeAll() {
        boolean allAgree = signUpModel.isAllAgree();
        signUpViewModel.toggleAllAgree(!allAgree);
        signUpModel.activateSignUpButtonByAgreeAll(!allAgree ? CheckPointsHolder.VALID : CheckPointsHolder.INVALID);

        boolean allValid = signUpModel.isAllValid();
        signUpViewModel.activateSignUpButton(allValid);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SignUp, AnalyticsValue.Action.AcceptAll);
    }

    @Click(R.id.btn_signup_confirm)
    void clickSignUp() {

        LogUtil.d("Click : clickSignUp");

        signUp();

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SignUp, AnalyticsValue.Action.SignUpNow);
    }

    @Background
    void signUp() {
        String email = signUpViewModel.getEmailText();
        String password = signUpViewModel.getPasswordText();
        String name = signUpViewModel.getNameText();
        String lang = LanguageUtil.getLanguage(SignUpActivity.this);

        signUpViewModel.showProgressWheel();
        try {
            signUpModel.requestSignUp(email, password, name, lang);

            signUpViewModel.dismissProgressWheel();

            signUpModel.trackSendEmailSuccess(email);

            signUpViewModel.requestSignUpVerify(email);
        } catch (RetrofitError e) {
            signUpViewModel.dismissProgressWheel();
            int errorCode = -1;
            if (e.getResponse() != null) {
                errorCode = e.getResponse().getStatus();

                String error = new String(((TypedByteArray) e.getResponse().getBody()).getBytes());
                if (error.contains("40001")) {
                    signUpViewModel.showErrorToast(getString(R.string.jandi_duplicate_email));
                }
            } else {
                signUpViewModel.showErrorToast(getString(R.string.err_network));
            }

            signUpModel.trackSendEmailFail(errorCode);
        }
    }
}
