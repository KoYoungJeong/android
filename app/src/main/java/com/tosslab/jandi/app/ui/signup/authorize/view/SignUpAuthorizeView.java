package com.tosslab.jandi.app.ui.signup.authorize.view;

/**
 * Created by tonyjs on 15. 5. 19..
 */
public interface SignUpAuthorizeView {
    String getAuthorizationCode();

    void setVerifyButtonEnabled(boolean valid);

    void showProgress();

    void hideProgress();

    void showInvalidAuthorizationCode(int count);
}
