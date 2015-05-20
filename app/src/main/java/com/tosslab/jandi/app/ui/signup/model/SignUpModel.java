package com.tosslab.jandi.app.ui.signup.model;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.signup.to.CheckPointsHolder;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.PasswordChecker;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Created by Steve SeongUg Jung on 14. 12. 24..
 */
@EBean
public class SignUpModel {

    CheckPointsHolder mCheckPointsHolder;

    @RestService
    JandiRestClient jandiRestClient;

    @AfterInject
    void initObject() {
        mCheckPointsHolder = new CheckPointsHolder();
    }


    public int checkPasswordStrength(String text) {
        return PasswordChecker.checkStrength(text.toString());
    }

    public boolean isValidEmail(String email) {
        return !FormatConverter.isInvalidEmailString(email);
    }

    public int getSignUpPasswordTextColor(int strength) {

        int textColorRes;
        if (strength >= PasswordChecker.AVERAGE) {
            textColorRes = R.color.jandi_text;
        } else {
            textColorRes = R.color.jandi_text_light;

        }

        return textColorRes;
    }

    public int getSignUpButtonState(int strength) {

        int validState;
        if (strength >= PasswordChecker.AVERAGE) {
            validState = CheckPointsHolder.VALID;
        } else {
            validState = CheckPointsHolder.INVALID;

        }

        return validState;
    }

    public int getEmailTextColor(boolean isValidEmail) {
        int textColorRes;
        if (isValidEmail) {
            textColorRes = R.color.jandi_text;
        } else {
            textColorRes = R.color.jandi_text_light;
        }

        return textColorRes;
    }

    public int getEmailValidValue(boolean isValidEmail) {
        int validState;
        if (isValidEmail) {
            validState = CheckPointsHolder.VALID;
        } else {
            validState = CheckPointsHolder.INVALID;
        }

        return validState;
    }

    public int getNameValidState(int nameLength) {
        int valid;
        if (nameLength > 0) {
            valid = CheckPointsHolder.VALID;
        } else {
            valid = CheckPointsHolder.INVALID;
        }

        return valid;
    }

    public boolean isAllAgree() {
        return mCheckPointsHolder.didAgreeAll == CheckPointsHolder.VALID;
    }

    public void activateSignUpButtonByEmail(int validState) {
        mCheckPointsHolder.isVaildEmail = validState;
    }

    public void activateSignUpButtonByPassword(int validState) {
        mCheckPointsHolder.isVaildPassword = validState;
    }

    public void activateSignUpButtonByName(int validState) {
        mCheckPointsHolder.isVaildName = validState;
    }

    public void activateSignUpButtonByAgreeAll(int validState) {
        mCheckPointsHolder.didAgreeAll = validState;
    }

    public boolean isAllValid() {
        return (mCheckPointsHolder.isVaildEmail == CheckPointsHolder.VALID
                && mCheckPointsHolder.isVaildName == CheckPointsHolder.VALID
                && mCheckPointsHolder.isVaildPassword == CheckPointsHolder.VALID
                && mCheckPointsHolder.didAgreeAll == CheckPointsHolder.VALID);
    }

    public ResAccountInfo requestSignUp(String email, String password, String name, String lang) throws JandiNetworkException {

        ReqSignUpInfo signUpInfo = new ReqSignUpInfo(email, password, name, lang);

        try {
            return jandiRestClient.signUpAccount(signUpInfo);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }

    }
}
