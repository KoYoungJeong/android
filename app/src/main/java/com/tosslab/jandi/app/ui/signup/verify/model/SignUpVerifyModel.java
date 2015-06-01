package com.tosslab.jandi.app.ui.signup.verify.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqAccountVerification;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.ui.signup.verify.to.VerifyNetworkException;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.EBean;
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

    public static final int AUTHORIZED = -1;

    public boolean isValidVerificationCode(String verificationCode) {
        return !TextUtils.isEmpty(verificationCode)
                && (TextUtils.getTrimmedLength(verificationCode) == 4);
    }

    public ResAccountInfo requestSignUpVerify(String email, String verificationCode)
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

}
