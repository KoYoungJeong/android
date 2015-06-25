package com.tosslab.jandi.app.ui.signup.verify.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelAccountAnalyticsClient;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqAccountVerification;
import com.tosslab.jandi.app.network.models.ResAccountActivate;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.ui.signup.verify.exception.VerifyNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import retrofit.RetrofitError;

/**
 * Created by tonyjs on 15. 5. 19..
 */
@EBean
public class SignUpVerifyModel {

    public static final int AUTHORIZED = -1;

    @RootContext
    Context context;

    public boolean isValidVerificationCode(String verificationCode) {
        return !TextUtils.isEmpty(verificationCode)
                && (TextUtils.getTrimmedLength(verificationCode) == 4);
    }

    public ResAccountActivate requestSignUpVerify(String email, String verificationCode)
            throws VerifyNetworkException {
        ReqAccountActivate accountActivate = new ReqAccountActivate(email, verificationCode);
        ResAccountActivate resAccountActivate = null;
        try {
            resAccountActivate = RequestApiManager.getInstance().activateAccountByMainRest(accountActivate);
        } catch (RetrofitError e) {
            e.printStackTrace();
            throw new VerifyNetworkException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resAccountActivate;
    }

    public ResCommon requestNewVerificationCode(String email) throws RetrofitError {
        ReqAccountVerification accountVerification = new ReqAccountVerification(email);
        ResCommon resCommon = null;
        try {
            resCommon = RequestApiManager.getInstance().accountVerificationByMainRest(accountVerification);
        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resCommon;
    }

    public void setAccountInfo(ResAccountActivate accountActivate) {
        ResAccountInfo accountInfo = accountActivate.getAccount();

        TokenUtil.saveTokenInfoByPassword(context, accountActivate.getAccessToken(),
                accountActivate.getRefreshToken(), accountActivate.getTokenType());

        JandiAccountDatabaseManager.getInstance(context).upsertAccountAllInfo(accountInfo);

        JandiPreference.setFirstLogin(context);

    }

    public void addTrackingCodeSignUp(ResAccountInfo accountInfo) {
        MixpanelAccountAnalyticsClient
                .getInstance(context, accountInfo.getId())
                .pageViewAccountCreateSuccess();

        MixpanelAccountAnalyticsClient mixpanelAccountAnalyticsClient =
                MixpanelAccountAnalyticsClient.getInstance(context, accountInfo.getId());
        mixpanelAccountAnalyticsClient.trackAccountSingingIn();
    }

}
