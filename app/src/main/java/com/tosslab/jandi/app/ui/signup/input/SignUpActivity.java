package com.tosslab.jandi.app.ui.signup.input;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.mixpanel.MixpanelAccountAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.signup.input.model.SignUpModel;
import com.tosslab.jandi.app.ui.signup.input.to.CheckPointsHolder;
import com.tosslab.jandi.app.ui.term.TermActivity;
import com.tosslab.jandi.app.ui.term.TermActivity_;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.LanguageUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;

/**
 * Created by justinygchoi on 14. 12. 11..
 */
@EActivity(R.layout.activity_signup)
public class SignUpActivity extends AppCompatActivity {

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

    }

    private void setUpActionBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        // Set up the action bar.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
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
    }

    @Click({R.id.btn_signup_agree_pp, R.id.ly_signup_agree_pp})
    void clickAgreePrivate() {
        signUpViewModel.togglePrivate();
        boolean isAllAgreed = signUpViewModel.checkAllAgree();
        signUpModel.activateSignUpButtonByAgreeAll(isAllAgreed ? CheckPointsHolder.VALID : CheckPointsHolder.INVALID);

        boolean allValid = signUpModel.isAllValid();
        signUpViewModel.activateSignUpButton(allValid);
    }

    @Click({R.id.btn_signup_agree_all, R.id.ly_signup_agree_all})
    void clickAgreeAll() {
        boolean allAgree = signUpModel.isAllAgree();
        signUpViewModel.toggleAllAgree(!allAgree);
        signUpModel.activateSignUpButtonByAgreeAll(!allAgree ? CheckPointsHolder.VALID : CheckPointsHolder.INVALID);

        boolean allValid = signUpModel.isAllValid();
        signUpViewModel.activateSignUpButton(allValid);
    }

    @Click(R.id.btn_signup_confirm)
    void clickSignUp() {

        LogUtil.d("Click : clickSignUp");

        signUp();
    }

    @Background
    void signUp() {
        String email = signUpViewModel.getEmailText();
        String password = signUpViewModel.getPasswordText();
        String name = signUpViewModel.getNameText();
        String lang = LanguageUtil.getLanguage(SignUpActivity.this);

        signUpViewModel.showProgressWheel();
        try {
            ResAccountInfo resAccountInfo = signUpModel.requestSignUp(email, password, name, lang);
            signUpViewModel.finishWithEmail(email);

            MixpanelAccountAnalyticsClient
                    .getInstance(SignUpActivity.this, resAccountInfo.getId())
                    .pageViewAccountCreateSuccess();

        } catch (JandiNetworkException e) {
            LogUtil.d(e.getErrorInfo() + " , Response Body : " + e.httpBody);
            if (e.errCode == 40001) {
                signUpViewModel.showErrorToast(getString(R.string.jandi_duplicate_email));
            } else {
                signUpViewModel.showErrorToast(getString(R.string.err_network));
            }
            signUpViewModel.dismissProgressWheel();
        } finally {
            signUpViewModel.dismissProgressWheel();
        }
    }
}
