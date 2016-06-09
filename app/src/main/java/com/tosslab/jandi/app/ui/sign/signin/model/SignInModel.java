package com.tosslab.jandi.app.ui.sign.signin.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.account.devices.DeviceApi;
import com.tosslab.jandi.app.network.client.account.password.AccountPasswordApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
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
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import dagger.Lazy;

/**
 * Created by tee on 16. 5. 25..
 */

public class SignInModel {

    Lazy<LoginApi> loginApi;

    Lazy<AccountApi> accountApi;

    Lazy<DeviceApi> deviceApi;

    public SignInModel(Lazy<LoginApi> loginApi, Lazy<AccountApi> accountApi, Lazy<DeviceApi> deviceApi) {
        this.loginApi = loginApi;
        this.accountApi = accountApi;
        this.deviceApi = deviceApi;
    }

    public ResAccessToken login(String myEmailId, String password) throws RetrofitException {
        ReqAccessToken passwordReqToken = ReqAccessToken.createPasswordReqToken(myEmailId, password);
        return loginApi.get().getAccessToken(passwordReqToken);
    }

    public ResCommon requestPasswordReset(String email) throws RetrofitException {
        return new AccountPasswordApi(RetrofitBuilder.getInstance())
                .resetPassword(new ReqAccountEmail(email, LanguageUtil.getLanguage()));
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

    public void trackSignInSuccess() {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.SignIn)
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.AutoSignIn, false)
                .build());
        AnalyticsUtil.flushSprinkler();
    }

    public void trackSignInFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.SignIn)
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());
        AnalyticsUtil.flushSprinkler();
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

}