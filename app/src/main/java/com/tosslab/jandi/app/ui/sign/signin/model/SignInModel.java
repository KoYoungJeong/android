package com.tosslab.jandi.app.ui.sign.signin.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.account.devices.DeviceApi;
import com.tosslab.jandi.app.network.client.account.password.AccountPasswordApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqSubscribeToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.LanguageUtil;
import com.tosslab.jandi.app.utils.TokenUtil;

import javax.inject.Inject;

import dagger.Lazy;

public class SignInModel {

    private Lazy<LoginApi> loginApi;

    private Lazy<AccountApi> accountApi;

    private Lazy<DeviceApi> deviceApi;
    private Lazy<AccountPasswordApi> accountPasswordApi;

    @Inject
    public SignInModel(Lazy<LoginApi> loginApi, Lazy<AccountApi> accountApi, Lazy<DeviceApi> deviceApi, Lazy<AccountPasswordApi> accountPasswordApi) {
        this.loginApi = loginApi;
        this.accountApi = accountApi;
        this.deviceApi = deviceApi;
        this.accountPasswordApi = accountPasswordApi;
    }

    public ResAccessToken login(String myEmailId, String password) throws RetrofitException {
        ReqAccessToken passwordReqToken = ReqAccessToken.createPasswordReqToken(myEmailId, password);
        return loginApi.get().getAccessToken(passwordReqToken);
    }

    public ResCommon requestPasswordReset(String email) throws RetrofitException {
        return accountPasswordApi.get().resetPassword(new ReqAccountEmail(email, LanguageUtil.getLanguage()));
    }

    public boolean saveTokenInfo(ResAccessToken accessToken) {
        return TokenUtil.saveTokenInfoByPassword(accessToken);
    }

    public boolean saveAccountInfo(ResAccountInfo resAccountInfo) {
        AccountUtil.removeDuplicatedTeams(resAccountInfo);
        return AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
    }

    public ResAccountInfo getAccountInfo() throws RetrofitException {
        return accountApi.get().getAccountInfo();
    }

    public boolean isValidEmailFormat(String email) {
        // ID 입력의 포멧 체크
        return !FormatConverter.isInvalidEmailString(email);
    }

    public boolean isEmptyEmail(String email) {
        return TextUtils.isEmpty(email);
    }

    public boolean isEmptyPassword(String password) {
        return TextUtils.isEmpty(password);
    }

    public void subscribePush(String deviceId) {
        try {
            deviceApi.get().updateSubscribe(deviceId, new ReqSubscribeToken(true));
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
    }

    public boolean isValidPassword(String password) {
        return password.length() >= 8;
    }

    public void updateLoginId(String email) {
        AccountRepository.getRepository().upsertLoginId(email);
    }
}
