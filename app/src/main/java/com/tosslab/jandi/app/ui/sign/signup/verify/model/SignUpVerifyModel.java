package com.tosslab.jandi.app.ui.sign.signup.verify.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.main.SignUpApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqAccountVerification;
import com.tosslab.jandi.app.network.models.ResAccountActivate;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.ui.sign.signup.verify.exception.VerifyNetworkException;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;

import javax.inject.Inject;

import dagger.Lazy;


public class SignUpVerifyModel {

    Lazy<SignUpApi> signUpApi;

    @Inject
    public SignUpVerifyModel(Lazy<SignUpApi> signUpApi) {
        this.signUpApi = signUpApi;
    }

    public boolean isValidVerificationCode(String verificationCode) {
        return !TextUtils.isEmpty(verificationCode)
                && (TextUtils.getTrimmedLength(verificationCode) == 4);
    }

    public ResAccountActivate requestSignUpVerify(String email, String verificationCode)
            throws VerifyNetworkException {
        ReqAccountActivate accountActivate = new ReqAccountActivate(email, verificationCode);
        ResAccountActivate resAccountActivate = null;
        try {
            resAccountActivate = signUpApi.get().activateAccount(accountActivate);
        } catch (RetrofitException e) {
            e.printStackTrace();
            throw new VerifyNetworkException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resAccountActivate;
    }

    public ResCommon requestNewVerificationCode(String email) throws RetrofitException {
        ReqAccountVerification accountVerification = new ReqAccountVerification(email);
        return signUpApi.get().accountVerification(accountVerification);
    }

    public void setAccountInfo(ResAccountActivate accountActivate) {
        ResAccountInfo accountInfo = accountActivate.getAccount();

        TokenUtil.saveTokenInfoByPassword(accountActivate);

        AccountUtil.removeDuplicatedTeams(accountInfo);
        AccountRepository.getRepository().upsertAccountAllInfo(accountInfo);

        JandiPreference.setFirstLogin(JandiApplication.getContext());

    }

}
