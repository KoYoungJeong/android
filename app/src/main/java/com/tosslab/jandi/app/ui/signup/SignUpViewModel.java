package com.tosslab.jandi.app.ui.signup;

import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.PasswordChecker;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterTextChange;
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

    private ProgressWheel mProgressWheel;

    static class CheckPointsHolder {
        static final int NOT_KNOW   = -1;
        static final int INVALID       = 0;
        static final int VALID         = 1;

        int isVaildPassword;
        int isVaildName;
        int isVaildEmail;
    }
    CheckPointsHolder mCheckPointsHolder;


    @AfterInject
    void initObject() {
        mProgressWheel = new ProgressWheel(activity);
        mProgressWheel.init();

        mCheckPointsHolder = new CheckPointsHolder();
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

        int textColorRes = R.color.jandi_main;
        if (isValidEmail) {
            activateSignUpButtonByEmail(CheckPointsHolder.VALID);
        } else {
            activateSignUpButtonByEmail(CheckPointsHolder.INVALID);
            textColorRes = R.color.dialog_title_text;
        }
        editTextSignUpEmail.setTextColor(activity.getResources().getColor(textColorRes));
    }

    @AfterTextChange(R.id.et_signup_password)
    void checkPasswordStrength() {
        int strength = PasswordChecker.checkStrength(
                editTextSignUpPassword.getEditableText().toString()
        );

        log.debug("Strength of password is " + strength);

        int textColorRes = R.color.jandi_main;
        if (strength >= PasswordChecker.AVERAGE) {
            activateSignUpButtonByPassword(CheckPointsHolder.VALID);
        } else {
            activateSignUpButtonByPassword(CheckPointsHolder.INVALID);
            textColorRes = R.color.dialog_title_text;
        }
        editTextSignUpPassword.setTextColor(activity.getResources().getColor(textColorRes));
    }

    void activateSignUpButtonByName(int isValidName) {
        activateSignUpButton(isValidName, CheckPointsHolder.NOT_KNOW, CheckPointsHolder.NOT_KNOW);
    }

    void activateSignUpButtonByEmail(int isValidEmail) {
        activateSignUpButton(CheckPointsHolder.NOT_KNOW, isValidEmail, CheckPointsHolder.NOT_KNOW);
    }

    void activateSignUpButtonByPassword(int isValidPassword) {
        activateSignUpButton(CheckPointsHolder.NOT_KNOW, CheckPointsHolder.NOT_KNOW, isValidPassword);
    }

    @SupposeUiThread
    void activateSignUpButton(int isValidName, int isValidEmail, int isValidPassword) {
        if (isValidEmail != CheckPointsHolder.NOT_KNOW)
            mCheckPointsHolder.isVaildEmail = isValidEmail;
        if (isValidName != CheckPointsHolder.NOT_KNOW)
            mCheckPointsHolder.isVaildName = isValidName;
        if (isValidPassword != CheckPointsHolder.NOT_KNOW)
            mCheckPointsHolder.isVaildPassword = isValidPassword;

        if (mCheckPointsHolder.isVaildEmail == CheckPointsHolder.VALID
                && mCheckPointsHolder.isVaildName == CheckPointsHolder.VALID
                && mCheckPointsHolder.isVaildPassword == CheckPointsHolder.VALID) {
            buttonSignUpConfirm.setSelected(true);
        } else {
            buttonSignUpConfirm.setSelected(false);
        }
    }
}
