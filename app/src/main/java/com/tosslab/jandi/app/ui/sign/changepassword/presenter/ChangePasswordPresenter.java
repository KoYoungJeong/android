package com.tosslab.jandi.app.ui.sign.changepassword.presenter;

/**
 * Created by tee on 2017. 4. 11..
 */

public interface ChangePasswordPresenter {

    boolean checkNewPasswordAgainValidation(String newPassword, String newAgainPassword);

    boolean checkNewPasswordValidation(String password);

    void setNewPassword(String oldPassword, String newPassword, String newPasswordAgain);

    void checkCanSendChangePassword(String oldPassword, String newPassword, String newPasswordAgain);

    void forgotPassword(String email);

    interface View {
        void showProgressWheel();

        void dismissProgressWheel();

        void removeErrorCurrentPassword();

        void removeErrorNewPassword();

        void removeErrorNewPasswordAgain();

        void showErrorWeakNewPassword();

        void showErrorNotSameNewPassword();

        void showErrorNotValidCurrentPassword();

        void setDoneButtonEnable(boolean visible);

        void showSuccessDialog();

        void showSuggestJoin(String email);

        void showNetworkErrorToast();

        void showFailPasswordResetToast();

        void showPasswordResetEmailSendSucsess();

    }
}
