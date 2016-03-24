package com.tosslab.jandi.app.ui.login.login.model;

import android.content.Context;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
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

import org.androidannotations.annotations.EBean;



/**
 * Created by Steve SeongUg Jung on 14. 12. 4..
 */
@EBean
public class IntroLoginModel {

    public ResAccessToken login(String myEmailId, String password) throws IOException {
        // 팀이 아무것도 없는 사용자일 경우의 에러 메시지
//        final int errStringResNotRegisteredId = R.string.err_login_unregistered_id;

        // Get Access Token
        ReqAccessToken passwordReqToken = ReqAccessToken.createPasswordReqToken(myEmailId, password);
        return RequestApiManager.getInstance().getAccessTokenByMainRest(passwordReqToken);
    }

    public boolean saveTokenInfo(ResAccessToken accessToken) {
        return TokenUtil.saveTokenInfoByPassword(accessToken);
    }

    public boolean saveAccountInfo(ResAccountInfo resAccountInfo) {
        return AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
    }

    public ResAccountInfo getAccountInfo() {
        return RequestApiManager.getInstance().getAccountInfoByMainRest();
    }

    public boolean isValidEmailFormat(String email) {
        // ID 입력의 포멧 체크
        return !FormatConverter.isInvalidEmailString(email);
    }

    public ResCommon requestPasswordReset(String email) {
        Context context = JandiApplication.getContext();
        return RequestApiManager.getInstance().resetPasswordByAccountPasswordApi(new ReqAccountEmail(email, LanguageUtil.getLanguage(context)));
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
