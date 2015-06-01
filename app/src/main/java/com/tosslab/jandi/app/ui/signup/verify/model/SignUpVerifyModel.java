package com.tosslab.jandi.app.ui.signup.verify.model;

import android.text.TextUtils;

import org.androidannotations.annotations.EBean;

/**
 * Created by tonyjs on 15. 5. 19..
 */
@EBean
public class SignUpVerifyModel {

    public static final int AUTHORIZED = -1;

    public boolean isValidVerificationCode(String verificationCode) {
        return !TextUtils.isEmpty(verificationCode)
                && (TextUtils.getTrimmedLength(verificationCode) == 4);
    }

    public int getVerifyTryCount(String verificationCode) {
        return AUTHORIZED;
    }

}
