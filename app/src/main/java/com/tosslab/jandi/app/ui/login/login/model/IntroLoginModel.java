package com.tosslab.jandi.app.ui.login.login.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.database.JandiDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiAuthClient;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.team.select.model.AccountInfoRequest;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

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

    @SupposeBackground
    public int startLogin(String myEmailId, String password) {
        // 팀이 아무것도 없는 사용자일 경우의 에러 메시지
        final int errStringResNotRegisteredId = R.string.err_login_unregistered_id;

        try {

            // Get Access Token
            ReqAccessToken passwordReqToken = ReqAccessToken.createPasswordReqToken(myEmailId, password);
            ResAccessToken accessToken = jandiRestClient.getAccessToken(passwordReqToken);

            if (accessToken != null && !TextUtils.isEmpty(accessToken.getAccessToken()) && !TextUtils.isEmpty(accessToken.getRefreshToken())) {
                // Save Token & Get TeamList
                TokenUtil.saveTokenInfoByPassword(context, accessToken);

                AccountInfoRequest accountInfoRequest = AccountInfoRequest.create(context, jandiRestClient);
                RequestManager<ResAccountInfo> requestManager = RequestManager.newInstance(context, accountInfoRequest);
                ResAccountInfo resAccountInfo = requestManager.request();

                JandiDatabaseManager.getInstance(context).upsertAccountInfo(resAccountInfo);
                JandiDatabaseManager.getInstance(context).upsertAccountEmail(resAccountInfo.getEmails());
                JandiDatabaseManager.getInstance(context).upsertAccountTeams(resAccountInfo.getMemberships());
                JandiDatabaseManager.getInstance(context).upsertAccountDevices(resAccountInfo.getDevices());

                return HttpStatus.OK.value();
            } else {
                // Login Fail
                throw new Exception("Login Fail");
            }

        } catch (JandiNetworkException e) {
            return e.httpStatusCode;
        } catch (Exception e) {
            log.error(e.toString(), e);
            return 400;
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
}
