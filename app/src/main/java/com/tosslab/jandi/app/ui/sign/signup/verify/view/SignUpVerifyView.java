package com.tosslab.jandi.app.ui.sign.signup.verify.view;

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

    void moveToSelectTeam();

    void changeExplainText();

    void clearVerifyCode();
}
