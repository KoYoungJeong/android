package com.tosslab.jandi.app.ui.signup.verify.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.mixpanel.MixpanelAccountAnalyticsClient;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqAccountVerification;
import com.tosslab.jandi.app.network.models.ResAccountActivate;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.ui.signup.verify.to.VerifyNetworkException;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.rest.RestService;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Created by tonyjs on 15. 5. 19..
 */
@EBean
public class SignUpVerifyModel {

    @RestService
    JandiRestClient restClient;

    @RootContext
    Context context;

    public static final int AUTHORIZED = -1;

    public boolean isValidVerificationCode(String verificationCode) {
        return !TextUtils.isEmpty(verificationCode)
                && (TextUtils.getTrimmedLength(verificationCode) == 4);
    }

    public ResAccountActivate requestSignUpVerify(String email, String verificationCode)
            throws VerifyNetworkException {
        ReqAccountActivate accountActivate = new ReqAccountActivate(email, verificationCode);

        try {
            return restClient.activateAccount(accountActivate);
        } catch (HttpStatusCodeException e) {
            throw new VerifyNetworkException(e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new VerifyNetworkException(
                    new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public ResCommon requestNewVerificationCode(String email) throws JandiNetworkException {
        ReqAccountVerification accountVerification = new ReqAccountVerification(email);

        try {
            return restClient.accountVerification(accountVerification);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new JandiNetworkException(
                    new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public void setResult(ResAccountActivate accountActivate) {
        ResAccountInfo accountInfo = accountActivate.getAccount();

        MixpanelAccountAnalyticsClient
                .getInstance(context, accountInfo.getId())
                .pageViewAccountCreateSuccess();

        TokenUtil.saveTokenInfoByPassword(context, accountActivate.getAccessToken(),
                accountActivate.getRefreshToken(), accountActivate.getTokenType());

        JandiAccountDatabaseManager.getInstance(context).upsertAccountAllInfo(accountInfo);

        JandiPreference.setFirstLogin(context);

        MixpanelAccountAnalyticsClient mixpanelAccountAnalyticsClient =
                MixpanelAccountAnalyticsClient.getInstance(context, accountInfo.getId());
        mixpanelAccountAnalyticsClient.trackAccountSingingIn();
    }

}
