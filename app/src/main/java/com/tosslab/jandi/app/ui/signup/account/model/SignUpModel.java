package com.tosslab.jandi.app.ui.signup.account.model;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.ui.signup.account.to.CheckPointsHolder;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.PasswordChecker;
import com.tosslab.jandi.app.utils.analytics.GoogleAnalyticsUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;

import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 14. 12. 24..
 */
@EBean
public class SignUpModel {

    CheckPointsHolder mCheckPointsHolder;

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

    public ResCommon requestSignUp(String email, String password, String name, String lang) throws RetrofitError {

        ReqSignUpInfo signUpInfo = new ReqSignUpInfo(email, password, name, lang);

        return RequestApiManager.getInstance().signUpAccountByMainRest(signUpInfo);

    }

    public void trackSendEmailSuccess(String email) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.SendAccountVerificationMail)
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.Email, email)
                        .build());

        GoogleAnalyticsUtil.sendEvent(Event.SendAccountVerificationMail.name(), "ResponseSuccess");
    }

    public void trackSendEmailFail(int errorCode) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.SendAccountVerificationMail)
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.ErrorCode, errorCode)
                        .build());

        GoogleAnalyticsUtil.sendEvent(Event.SendAccountVerificationMail.name(), "ResponseFail");
    }

}
