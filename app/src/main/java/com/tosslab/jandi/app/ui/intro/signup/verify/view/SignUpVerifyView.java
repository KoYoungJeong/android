package com.tosslab.jandi.app.ui.intro.signup.verify.view;

/**
 * Created by tonyjs on 15. 5. 19..
 */
public interface SignUpVerifyView {

    void showProgress();

    void hideProgress();

    void showInvalidVerificationCode(int count);

    void showExpiredVerificationCode();

    void showToast(String msg);

    void showErrorToast(String msg);

    void moveToAccountHome();

    void changeExplainText();

    void clearVerifyCode();
}
