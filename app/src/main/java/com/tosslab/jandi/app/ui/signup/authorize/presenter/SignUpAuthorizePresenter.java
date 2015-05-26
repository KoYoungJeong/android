package com.tosslab.jandi.app.ui.signup.authorize.presenter;

import com.tosslab.jandi.app.ui.signup.authorize.model.SignUpAuthorizeModel;
import com.tosslab.jandi.app.ui.signup.authorize.view.SignUpAuthorizeView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

/**
 * Created by tonyjs on 15. 5. 19..
 */
@EBean
public class SignUpAuthorizePresenter {

    @Bean
    SignUpAuthorizeModel model;

    SignUpAuthorizeView view;

    public void setView(SignUpAuthorizeView signUpVerifyView) {
        view = signUpVerifyView;
    }

    public void validateAuthorizationCode() {
        String verifyCode = view.getAuthorizationCode();
        boolean verified = model.isValidAuthorizationCode(verifyCode);
        view.setVerifyButtonEnabled(verified);
    }

    @Background
    public void verifyAuthorizationCode() {
        view.showProgress();

        String authorizationCode = view.getAuthorizationCode();
        int authorizeTryCount = model.getAuthorizeTryCount(authorizationCode);
        switch (authorizeTryCount) {
            case SignUpAuthorizeModel.AUTHORIZED:
                view.hideProgress();
                break;
        }
    }
}
