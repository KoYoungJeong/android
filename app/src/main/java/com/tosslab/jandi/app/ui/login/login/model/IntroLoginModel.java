package com.tosslab.jandi.app.ui.login.login.model;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.account.password.AccountPasswordApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.LanguageUtil;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;

import javax.inject.Inject;

import dagger.Lazy;


/**
 * Created by Steve SeongUg Jung on 14. 12. 4..
 */
@EBean
public class IntroLoginModel {

    @Inject
    Lazy<LoginApi> loginApi;

    @Inject
    Lazy<AccountApi> accountApi;

    @AfterInject
    void initObect() {
        DaggerApiClientComponent
                .builder()
                .build()
                .inject(this);
    }

    public ResAccessToken login(String myEmailId, String password) throws RetrofitException {
        // 팀이 아무것도 없는 사용자일 경우의 에러 메시지
//        final int errStringResNotRegisteredId = R.string.err_login_unregistered_id;

        // Get Access Token
        ReqAccessToken passwordReqToken = ReqAccessToken.createPasswordReqToken(myEmailId, password);
        return loginApi.get().getAccessToken(passwordReqToken);
    }

    public boolean saveTokenInfo(ResAccessToken accessToken) {
        return TokenUtil.saveTokenInfoByPassword(accessToken);
    }

    public boolean saveAccountInfo(ResAccountInfo resAccountInfo) {
        return AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
    }

    public ResAccountInfo getAccountInfo() throws RetrofitException {
        return accountApi.get().getAccountInfo();
    }

    public boolean isValidEmailFormat(String email) {
        // ID 입력의 포멧 체크
        return !FormatConverter.isInvalidEmailString(email);
    }

    public ResCommon requestPasswordReset(String email) throws RetrofitException {
        return new AccountPasswordApi().resetPassword(new ReqAccountEmail(email, LanguageUtil.getLanguage()));
    }

    public void trackSignInSuccess() {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.SignIn)
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.AutoSignIn, false)
                        .build())
                .flush();
    }

    public void trackSignInFail(int errorCode) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.SignIn)
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.ErrorCode, errorCode)
                        .build())
                .flush();

    }
}
