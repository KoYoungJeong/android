package com.tosslab.jandi.app.ui.signup;

import android.app.Activity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.PasswordChecker;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

/**
 * Created by justinygchoi on 14. 12. 12..
 */
@EBean
public class SignUpViewModel {
    private final Logger log = Logger.getLogger(SignUpViewModel.class);

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
    LinearLayout layoutSignUpAgreeTos;
    @ViewById(R.id.txt_signup_agree_tos)
    TextView textViewSignUpAgreeTos;
    @ViewById(R.id.ly_signup_agree_pp)
    LinearLayout layoutSignUpAgreePp;
    @ViewById(R.id.txt_signup_agree_pp)
    TextView textViewSignUpAgreePp;

    @ViewById(R.id.ly_password_strength_barometer)
    PasswordStrengthBarometerView passwordStrengthBarometerView;

    private ProgressWheel mProgressWheel;

    static class CheckPointsHolder {
        static final int NOT_KNOW   = -1;
        static final int INVALID    = 0;
        static final int VALID      = 1;

        int isVaildPassword;
        int isVaildName;
        int isVaildEmail;
        int didAgreeAll;
    }
    CheckPointsHolder mCheckPointsHolder;


    @AfterInject
    void initObject() {
        mProgressWheel = new ProgressWheel(activity);
        mProgressWheel.init();

        mCheckPointsHolder = new CheckPointsHolder();
    }

    @AfterViews
    void setLinkText() {
        textViewSignUpAgreeTos.setMovementMethod(LinkMovementMethod.getInstance());
        textViewSignUpAgreePp.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @AfterTextChange(R.id.et_signup_name)
    void checkName() {
        if (editTextSignUpName.length() > 0) {
            activateSignUpButtonByName(CheckPointsHolder.VALID);
        } else {
            activateSignUpButtonByName(CheckPointsHolder.INVALID);
        }
    }

    @AfterTextChange(R.id.et_signup_email)
    void checkValidEmail() {
        // TODO 로직을 부정으로 하면 헷갈림.
        boolean isValidEmail = !FormatConverter.isInvalidEmailString(
                editTextSignUpEmail.getEditableText().toString()
        );

        int textColorRes = R.color.jandi_text;
        if (isValidEmail) {
            activateSignUpButtonByEmail(CheckPointsHolder.VALID);
        } else {
            activateSignUpButtonByEmail(CheckPointsHolder.INVALID);
            textColorRes = R.color.jandi_text_light;
        }
        editTextSignUpEmail.setTextColor(activity.getResources().getColor(textColorRes));
    }

    @AfterTextChange(R.id.et_signup_password)
    void checkPasswordStrength() {
        int strength = PasswordChecker.checkStrength(
                editTextSignUpPassword.getEditableText().toString()
        );
        passwordStrengthBarometerView.setVisibility(View.VISIBLE);
        passwordStrengthBarometerView.setStrengthBarometer(strength);

        int textColorRes = R.color.jandi_text;
        if (strength >= PasswordChecker.AVERAGE) {
            activateSignUpButtonByPassword(CheckPointsHolder.VALID);
        } else {
            activateSignUpButtonByPassword(CheckPointsHolder.INVALID);
            textColorRes = R.color.jandi_text_light;

        }
        editTextSignUpPassword.setTextColor(activity.getResources().getColor(textColorRes));
    }

    void activateSignUpButtonByName(int isValidName) {
        activateSignUpButton(isValidName, CheckPointsHolder.NOT_KNOW, CheckPointsHolder.NOT_KNOW, CheckPointsHolder.NOT_KNOW);
    }

    void activateSignUpButtonByEmail(int isValidEmail) {
        activateSignUpButton(CheckPointsHolder.NOT_KNOW, isValidEmail, CheckPointsHolder.NOT_KNOW, CheckPointsHolder.NOT_KNOW);
    }

    void activateSignUpButtonByPassword(int isValidPassword) {
        activateSignUpButton(CheckPointsHolder.NOT_KNOW, CheckPointsHolder.NOT_KNOW, isValidPassword, CheckPointsHolder.NOT_KNOW);
    }

    void activateSignUpButtonByAgreeAll(int didAgreeAll) {
        activateSignUpButton(CheckPointsHolder.NOT_KNOW, CheckPointsHolder.NOT_KNOW, CheckPointsHolder.NOT_KNOW, didAgreeAll);
    }

    @SupposeUiThread
    void activateSignUpButton(int isValidName, int isValidEmail, int isValidPassword, int didAgreeAll) {
        if (isValidEmail != CheckPointsHolder.NOT_KNOW)
            mCheckPointsHolder.isVaildEmail = isValidEmail;
        if (isValidName != CheckPointsHolder.NOT_KNOW)
            mCheckPointsHolder.isVaildName = isValidName;
        if (isValidPassword != CheckPointsHolder.NOT_KNOW)
            mCheckPointsHolder.isVaildPassword = isValidPassword;
        if (didAgreeAll != CheckPointsHolder.NOT_KNOW)
            mCheckPointsHolder.didAgreeAll = didAgreeAll;

        if (mCheckPointsHolder.isVaildEmail == CheckPointsHolder.VALID
                && mCheckPointsHolder.isVaildName == CheckPointsHolder.VALID
                && mCheckPointsHolder.isVaildPassword == CheckPointsHolder.VALID
                && mCheckPointsHolder.didAgreeAll == CheckPointsHolder.VALID) {
            buttonSignUpConfirm.setSelected(true);
        } else {
            buttonSignUpConfirm.setSelected(false);
        }
    }

    @Click({R.id.btn_signup_agree_all, R.id.ly_signup_agree_all})
    void agreeAll() {
        if (layoutSignUpAgreeAll.isSelected()) {
            toggleAllAgree(false);
        } else {
            toggleAllAgree(true);
        }
    }

    @Click({R.id.btn_signup_agree_tos, R.id.ly_signup_agree_tos})
    void agreeTos() {
        layoutSignUpAgreeTos.setSelected(!layoutSignUpAgreeTos.isSelected());
        checkAllAgree();
    }

    @Click({R.id.btn_signup_agree_pp, R.id.ly_signup_agree_pp})
    void agreePp() {
        layoutSignUpAgreePp.setSelected(!layoutSignUpAgreePp.isSelected());
        checkAllAgree();
    }

    void checkAllAgree() {
        if (layoutSignUpAgreePp.isSelected() && layoutSignUpAgreeTos.isSelected()) {
            toggleAllAgree(true);
        } else {
            layoutSignUpAgreeAll.setSelected(false);
            activateSignUpButtonByAgreeAll(CheckPointsHolder.INVALID);
        }
    }

    void toggleAllAgree(boolean didAgreeAll) {
        if (didAgreeAll) {
            activateSignUpButtonByAgreeAll(CheckPointsHolder.VALID);
        } else {
            activateSignUpButtonByAgreeAll(CheckPointsHolder.INVALID);
        }
        layoutSignUpAgreeAll.setSelected(didAgreeAll);
        layoutSignUpAgreeTos.setSelected(didAgreeAll);
        layoutSignUpAgreePp.setSelected(didAgreeAll);
    }
}
