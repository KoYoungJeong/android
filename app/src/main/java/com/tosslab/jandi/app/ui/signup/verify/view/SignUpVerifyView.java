package com.tosslab.jandi.app.ui.signup.verify.view;

import com.tosslab.jandi.app.network.models.ResAccountInfo;

/**
 * Created by tonyjs on 15. 5. 19..
 */
public interface SignUpVerifyView {
    String getVerificationCode();

    void setVerifyButtonEnabled(boolean valid);

    void showProgress();

    void hideProgress();

    void showInvalidVerificationCode(int count);

    void hideInvalidVerificationCode();

    void setInvalidateTextColor();

    void setValidateTextColor();

    void showErrorToast(String msg);

    void setResult(ResAccountInfo accountInfo);
}
