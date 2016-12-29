package com.tosslab.jandi.app.ui.sign.signup.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.client.main.SignUpApi;
import com.tosslab.jandi.app.network.client.validation.ValidationApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.validation.EmailTypo;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.PasswordChecker;

import javax.inject.Inject;

import dagger.Lazy;

public class SignUpModel {

    Lazy<SignUpApi> signUpApi;
    private final Lazy<ValidationApi> validationApi;

    @Inject
    public SignUpModel(Lazy<SignUpApi> signUpApi, Lazy<ValidationApi> validationApi) {
        this.signUpApi = signUpApi;
        this.validationApi = validationApi;
    }

    public ResCommon requestSignUp(String email, String password, String name, String lang) throws RetrofitException {
        ReqSignUpInfo signUpInfo = new ReqSignUpInfo(email, password, name, lang);
        return signUpApi.get().signUpAccount(signUpInfo);
    }

    public boolean isValidEmailFormat(String email) {
        return !FormatConverter.isInvalidEmailString(email);
    }

    public boolean isEmptyEmail(String email) {
        return TextUtils.isEmpty(email);
    }

    public boolean isEmptyPassword(String password) {
        return TextUtils.isEmpty(password);
    }

    public boolean isValidLengthPassword(String password) {
        return password.length() >= 8;
    }

    public boolean isValidCharacterPassword(String password) {
        return PasswordChecker.checkStrength(password) >= PasswordChecker.AVERAGE;

    }

    public EmailTypo getEmailTypo() throws RetrofitException {
        return validationApi.get().getEmailTypo();
    }

}
