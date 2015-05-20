package com.tosslab.jandi.app.ui.signup.authorize.model;

import android.text.TextUtils;

import org.androidannotations.annotations.EBean;

/**
 * Created by tonyjs on 15. 5. 19..
 */
@EBean
public class SignUpAuthorizeModel {

    public static final int AUTHORIZED = -1;

    public boolean isValidVerifyCode(String verifyCode) {
        return TextUtils.isEmpty(verifyCode) && (TextUtils.getTrimmedLength(verifyCode) == 4);
    }

    public int getAuthorizeTryCount(String authorizationCode) {
        return AUTHORIZED;
    }

}
