package com.tosslab.jandi.app.ui.signup.verify.presenter;

import com.tosslab.jandi.app.ui.signup.verify.model.SignUpVerifyModel;
import com.tosslab.jandi.app.ui.signup.verify.view.SignUpVerifyView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

/**
 * Created by tonyjs on 15. 5. 19..
 */
@EBean
public class SignUpVerifyPresenter {

    @Bean
    SignUpVerifyModel model;

    SignUpVerifyView view;

    public void setView(SignUpVerifyView signUpVerifyView) {
        view = signUpVerifyView;
    }

    public void validateVerificationCode() {
        String verifyCode = view.getVerificationCode();
        boolean verified = model.isValidVerificationCode(verifyCode);
        view.setVerifyButtonEnabled(verified);
    }

    @Background
    public void verifyVerificationCode() {
        view.showProgress();

        String verificationCode = view.getVerificationCode();
        int verifyTryCount = model.getVerifyTryCount(verificationCode);
        switch (verifyTryCount) {
            case SignUpVerifyModel.AUTHORIZED:
                view.hideProgress();
                break;
        }
    }
}
