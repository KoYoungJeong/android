package com.tosslab.jandi.app.ui.sign.changepassword.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.client.account.password.AccountPasswordApi;
import com.tosslab.jandi.app.network.client.settings.ChangePasswordApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.utils.LanguageUtil;
import com.tosslab.jandi.app.utils.PasswordChecker;

import javax.inject.Inject;

import dagger.Lazy;

/**
 * Created by tee on 2017. 4. 11..
 */

public class ChangePasswordModel {

    private Lazy<ChangePasswordApi> changePasswordApi;

    private Lazy<AccountPasswordApi> accountPasswordApi;

    @Inject
    public ChangePasswordModel(Lazy<ChangePasswordApi> changePasswordApi,
                               Lazy<AccountPasswordApi> accountPasswordApi) {
        this.changePasswordApi = changePasswordApi;
        this.accountPasswordApi = accountPasswordApi;
    }

    public boolean isValidCharacterPassword(String password) {
        return PasswordChecker.checkStrength(password) >= PasswordChecker.AVERAGE;
    }

    public boolean isEmptyPassword(String password) {
        return TextUtils.isEmpty(password);
    }

    public boolean isValidLengthPassword(String password) {
        return password.length() >= 8;
    }

    public Boolean changePasswordApi(String oldPassword, String newPassword) {
        try {
            changePasswordApi.get().changePassword(oldPassword, newPassword);
        } catch (RetrofitException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public ResCommon requestPasswordReset(String email) throws RetrofitException {
        return accountPasswordApi.get().resetPassword(new ReqAccountEmail(email, LanguageUtil.getLanguage()));
    }
}
