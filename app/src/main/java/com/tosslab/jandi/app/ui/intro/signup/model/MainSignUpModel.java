package com.tosslab.jandi.app.ui.intro.signup.model;

import com.tosslab.jandi.app.network.client.main.SignUpApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.PasswordChecker;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import dagger.Lazy;

public class MainSignUpModel {

    Lazy<SignUpApi> signUpApi;

    public MainSignUpModel(Lazy<SignUpApi> signUpApi) {
        this.signUpApi = signUpApi;
    }

    public ResCommon requestSignUp(String email, String password, String name, String lang) throws RetrofitException {
        ReqSignUpInfo signUpInfo = new ReqSignUpInfo(email, password, name, lang);
        return signUpApi.get().signUpAccount(signUpInfo);
    }

    public void trackSendEmailSuccess(String email) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.SendAccountVerificationMail)
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.Email, email)
                .build());
    }

    public void trackSendEmailFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.SendAccountVerificationMail)
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());
    }

    public boolean isValidEmailFormat(String email) {
        return !FormatConverter.isInvalidEmailString(email);
    }

    public boolean isEmptyEmail(String email) {
        return email.length() <= 0;
    }

    public boolean isEmptyPassword(String password) {
        return password.length() <= 0;
    }

    public boolean isValidLengthPassword(String password) {
        return password.length() >= 8;
    }

    public boolean isValidCharacterPassword(String password) {
        return PasswordChecker.checkStrength(password) >= PasswordChecker.AVERAGE;

    }

    public boolean isEmptyName(String name) {
        return name.length() <= 0;
    }

}
