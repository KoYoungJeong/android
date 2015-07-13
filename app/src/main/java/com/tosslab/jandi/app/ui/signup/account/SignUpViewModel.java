package com.tosslab.jandi.app.ui.signup.account;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.login.login.IntroLoginFragment;
import com.tosslab.jandi.app.ui.signup.account.to.CheckPointsHolder;
import com.tosslab.jandi.app.ui.signup.verify.SignUpVerifyActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by justinygchoi on 14. 12. 12..
 */
@EBean
public class SignUpViewModel {

    @RootContext
    Activity activity;
    @ViewById(R.id.et_signup_email)
    EditText editTextSignUpEmail;
    @ViewById(R.id.et_signup_name)
    EditText editTextSignUpName;
    @ViewById(R.id.et_signup_password)
    EditText editTextSignUpPassword;
    @ViewById(R.id.btn_signup_confirm)
    Button buttonSignUpConfirm;
    @ViewById(R.id.ly_signup_agree_all)
    LinearLayout layoutSignUpAgreeAll;
    @ViewById(R.id.ly_signup_agree_tos)
    LinearLayout layoutSignUpAgreeEULA;
    @ViewById(R.id.txt_signup_agree_tos)
    TextView textViewSignUpAgreeTos;
    @ViewById(R.id.ly_signup_agree_pp)
    LinearLayout layoutSignUpAgreePrivate;
    @ViewById(R.id.txt_signup_agree_pp)
    TextView textViewSignUpAgreePp;

    @ViewById(R.id.tv_signup_name_valid)
    TextView nameValidView;
    @ViewById(R.id.tv_signup_email_valid)
    TextView emailValidView;
    @ViewById(R.id.tv_signup_password_valid)
    TextView passwordValidView;

    @ViewById(R.id.ly_password_strength_barometer)
    PasswordStrengthBarometerView passwordStrengthBarometerView;
    private ProgressWheel mProgressWheel;
    private String defaultEmail;

    @AfterInject
    void initObject() {
        mProgressWheel = new ProgressWheel(activity);
    }

    @AfterViews
    void setLinkText() {
        textViewSignUpAgreeTos.setMovementMethod(LinkMovementMethod.getInstance());
        textViewSignUpAgreePp.setMovementMethod(LinkMovementMethod.getInstance());
        editTextSignUpEmail.setText(defaultEmail);
        editTextSignUpEmail.setSelection(!TextUtils.isEmpty(defaultEmail) ? defaultEmail.length() : 0);
    }

    @SupposeUiThread
    void activateSignUpButton(boolean isAllValid) {
        buttonSignUpConfirm.setEnabled(isAllValid);
    }

    void toggleEula() {
        layoutSignUpAgreeEULA.setSelected(!layoutSignUpAgreeEULA.isSelected());
    }

    void togglePrivate() {
        layoutSignUpAgreePrivate.setSelected(!layoutSignUpAgreePrivate.isSelected());
    }

    boolean checkAllAgree() {
        boolean didAgreeAll;

        if (layoutSignUpAgreePrivate.isSelected() && layoutSignUpAgreeEULA.isSelected()) {
            didAgreeAll = true;
            toggleAllAgree(didAgreeAll);
        } else {
            didAgreeAll = false;
            layoutSignUpAgreeAll.setSelected(didAgreeAll);
        }

        return didAgreeAll;
    }

    void toggleAllAgree(boolean didAgreeAll) {
        layoutSignUpAgreeAll.setSelected(didAgreeAll);
        layoutSignUpAgreeEULA.setSelected(didAgreeAll);
        layoutSignUpAgreePrivate.setSelected(didAgreeAll);
    }

    public void setStrengthBarometer(int strength) {
        passwordStrengthBarometerView.setVisibility(View.VISIBLE);
        passwordStrengthBarometerView.setStrengthBarometer(strength);
    }

    public String getEmailText() {
        return editTextSignUpEmail.getText().toString();
    }

    public void setEmailText(String email) {
        editTextSignUpEmail.setText(email);
        editTextSignUpEmail.setSelection(email.length());
    }

    public String getPasswordText() {
        return editTextSignUpPassword.getText().toString();
    }

    public String getNameText() {
        return editTextSignUpName.getText().toString();
    }


    @UiThread
    public void showErrorToast(String message) {
        ColoredToast.showWarning(activity, message);
    }

    @UiThread
    public void requestSignUpVerify(String email) {
        SignUpVerifyActivity_.intent(activity)
                .email(email)
                .start();
    }

    public void setDefaultEmail(String email) {
        this.defaultEmail = email;
    }

    public void toggleNameAlert(int valid) {

        if (valid == CheckPointsHolder.VALID) {
            nameValidView.setVisibility(View.GONE);
        } else {
            nameValidView.setVisibility(View.VISIBLE);
        }

    }

    public void toggleEmailAlert(boolean isValidEmail) {
        if (isValidEmail) {
            emailValidView.setVisibility(View.GONE);
        } else {
            emailValidView.setVisibility(View.VISIBLE);
        }

    }

    public void togglePasswordAlert(int validState) {
        if (validState == CheckPointsHolder.VALID) {
            passwordValidView.setVisibility(View.GONE);
        } else {
            passwordValidView.setVisibility(View.VISIBLE);
        }
    }

    @UiThread
    public void showProgressWheel() {
        dismissProgressWheel();
        if (mProgressWheel != null) {
            mProgressWheel.show();
        }
    }

    @UiThread
    public void dismissProgressWheel() {
        if (mProgressWheel != null && mProgressWheel.isShowing()) {
            mProgressWheel.dismiss();
        }
    }


}
