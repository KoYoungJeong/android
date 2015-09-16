package com.tosslab.jandi.app.ui.login.login.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
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
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.SupposeUiThread;

import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 14. 12. 4..
 */
@EBean
public class IntroLoginModel {

    @RootContext
    Context context;

    private boolean isValidEmail;
    private boolean isValidPassword;


    @SupposeBackground
    public int startLogin(String myEmailId, String password) {
        // 팀이 아무것도 없는 사용자일 경우의 에러 메시지
//        final int errStringResNotRegisteredId = R.string.err_login_unregistered_id;

        try {
            // Get Access Token
            ReqAccessToken passwordReqToken = ReqAccessToken.createPasswordReqToken(myEmailId, password);
            ResAccessToken accessToken = RequestApiManager.getInstance().getAccessTokenByMainRest(passwordReqToken);

            if (accessToken != null && !TextUtils.isEmpty(accessToken.getAccessToken()) && !TextUtils.isEmpty(accessToken.getRefreshToken())) {
                // Save Token & Get TeamList
                TokenUtil.saveTokenInfoByPassword(accessToken);
                ResAccountInfo resAccountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
                AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
                return JandiConstants.NETWORK_SUCCESS;
            } else {
                // Login Fail
                throw new Exception("Login Fail");
            }

        } catch (RetrofitError e) {
            if (e.getResponse() != null) {
                return e.getResponse().getStatus();
            }
            return JandiConstants.NetworkError.BAD_REQUEST;
        } catch (Exception e) {
            LogUtil.e(e.toString(), e);
            return JandiConstants.NetworkError.BAD_REQUEST;
        }
    }

    @SupposeUiThread
    public boolean isValidEmailFormat(String email) {
        // ID 입력의 포멧 체크
        if (FormatConverter.isInvalidEmailString(email)) {
            return false;
        }
        return true;
    }

    public String getEmailHost(String signedEmail) {
        if (TextUtils.isEmpty(signedEmail)) {
            return "";
        }

        int i = signedEmail.indexOf("@");

        if (i > 0 && i < signedEmail.length()) {
            return signedEmail.substring(i + 1);
        } else {
            return "";
        }
    }

    public ResCommon requestPasswordReset(String email) {
        return RequestApiManager.getInstance().resetPasswordByAccountPasswordApi(new ReqAccountEmail(email, LanguageUtil.getLanguage(context)));
    }

    public void setValidEmail(boolean isValidEmail) {
        this.isValidEmail = isValidEmail;
    }

    public void setValidPassword(boolean isValidPassword) {
        this.isValidPassword = isValidPassword;
    }

    public boolean isValidEmailPassword() {
        return isValidEmail && isValidPassword;
    }

    public void trackSignInSuccess() {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.SignIn)
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.AutoSignIn, false)
                        .build())
                .flush();

        AnalyticsUtil.sendEvent(Event.SignIn.name(), "ResponseSuccess");
    }

    public void trackSignInFail(int errorCode) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.SignIn)
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.ErrorCode, errorCode)
                        .build())
                .flush();

        AnalyticsUtil.sendEvent(Event.SignIn.name(), "ResponseFail");

    }
}
