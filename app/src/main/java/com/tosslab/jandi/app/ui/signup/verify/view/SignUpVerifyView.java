package com.tosslab.jandi.app.ui.signup.verify.view;

/**
 * Created by tonyjs on 15. 5. 19..
 */
public interface SignUpVerifyView {
    String getVerificationCode();

    void setVerifyButtonEnabled(boolean valid);

    void showProgress();

    void hideProgress();

    void showInvalidVerificationCode(int count);
}
