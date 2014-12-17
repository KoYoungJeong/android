package com.tosslab.jandi.app.ui.login.login.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.client.JandiAuthClient;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;

/**
 * Created by Steve SeongUg Jung on 14. 12. 4..
 */
@EBean
public class IntroLoginModel {

    private final Logger log = Logger.getLogger(IntroLoginModel.class);

    @Bean
    JandiAuthClient mJandiAuthClient;

    @RestService
    JandiRestClient jandiRestClient;

    @RootContext
    Context context;

    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Background
    public void createTeamInBackground(String myEmailId) {
        try {
            // 나의 팀 ID 획득
            ResCommon res = mJandiAuthClient.createTeam(myEmailId);
            if (callback != null) {
                callback.onCreateTeamSuccess();
            }
        } catch (JandiNetworkException e) {
            log.error("createTeamInBackground", e);
            createTeamFail(R.string.err_team_creation_failed);
        } catch (Exception e) {
            log.error("createTeamInBackground", e);
            createTeamFail(R.string.err_network);
        }
    }

    private void createTeamFail(int errorStringResId) {
        if (callback != null) {
            callback.onCreateTeamFail(errorStringResId);
        }
    }

    @Background
    public void startLogin(String myEmailId, String password) {
        // 팀이 아무것도 없는 사용자일 경우의 에러 메시지
        final int errStringResNotRegisteredId = R.string.err_login_unregistered_id;

        try {

            // Get Access Token
            ReqAccessToken passwordReqToken = ReqAccessToken.createPasswordReqToken(myEmailId, password);
            ResAccessToken accessToken = jandiRestClient.getAccessToken(passwordReqToken);

            if (accessToken != null && !TextUtils.isEmpty(accessToken.getAccessToken()) && !TextUtils.isEmpty(accessToken.getRefreshToken())) {
                // Save Token & Get TeamList
                TokenUtil.saveTokenInfo(context, accessToken);

                if (callback != null) {
                    callback.onLoginSuccess(myEmailId);
                }
            } else {
                // Login Fail
                throw new Exception("Login Fail");
            }


            return;
        } catch (JandiNetworkException e) {
            int errorStringRes = R.string.err_network;
            if (e.errCode == JandiNetworkException.DATA_NOT_FOUND) {
                // 팀이 아무것도 없는 사용자일 경우
                errorStringRes = errStringResNotRegisteredId;
            }
            loginFail(errorStringRes);
        } catch (Exception e) {
            log.error(e.toString(), e);
            loginFail(R.string.err_network);
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

    private void loginFail(int errStringResNotRegisteredId) {
        if (callback != null) {
            callback.onLoginFail(errStringResNotRegisteredId);
        }
    }

    public interface Callback {

        void onCreateTeamSuccess();

        void onCreateTeamFail(int stringResId);

        void onLoginSuccess(String email);

        void onLoginFail(int errorStringResId);


    }
}
