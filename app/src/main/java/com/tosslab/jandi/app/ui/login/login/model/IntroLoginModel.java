package com.tosslab.jandi.app.ui.login.login.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.ui.team.select.model.AccountInfoRequest;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.LanguageUtil;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.SupposeUiThread;
import org.springframework.http.HttpStatus;

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
        final int errStringResNotRegisteredId = R.string.err_login_unregistered_id;

        try {

            // Get Access Token
            ReqAccessToken passwordReqToken = ReqAccessToken.createPasswordReqToken(myEmailId, password);
            ResAccessToken accessToken = RequestApiManager.getInstance().getAccessTokenByMainRest(passwordReqToken);

            if (accessToken != null && !TextUtils.isEmpty(accessToken.getAccessToken()) && !TextUtils.isEmpty(accessToken.getRefreshToken())) {
                // Save Token & Get TeamList
                TokenUtil.saveTokenInfoByPassword(context, accessToken);

                AccountInfoRequest accountInfoRequest = AccountInfoRequest.create(context);
                RequestManager<ResAccountInfo> requestManager = RequestManager.newInstance(context, accountInfoRequest);
                ResAccountInfo resAccountInfo = requestManager.request();

                JandiAccountDatabaseManager.getInstance(context).upsertAccountAllInfo(resAccountInfo);

                return HttpStatus.OK.value();
            } else {
                // Login Fail
                throw new Exception("Login Fail");
            }

        } catch (JandiNetworkException e) {
            return e.httpStatusCode;
        } catch (Exception e) {
            LogUtil.e(e.toString(), e);
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

}
