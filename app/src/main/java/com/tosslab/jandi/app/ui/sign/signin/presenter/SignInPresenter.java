package com.tosslab.jandi.app.ui.sign.signin.presenter;

/**
 * Created by tee on 16. 5. 25..
 */

public interface SignInPresenter {

    boolean checkEmailValidation(String email);

    boolean checkPasswordValidation(String password);

    void trySignIn(String email, String password, String captchaResponse);

    void forgotPassword(String email);

    interface View {

        void showErrorInsertEmail();

        void showErrorInvalidEmail();

        void removeErrorEmail();

        void showErrorInsertPassword();

        void showErrorInvalidPassword();

        void removeErrorPassword();

        void showErrorInvalidEmailOrPassword();

        void showNetworkErrorToast();

        void showProgressDialog();

        void dismissProgressDialog();

        void moveToTeamSelectionActivity(String myEmailId);

        void showSuccessPasswordResetToast();

        void showFailPasswordResetToast();

        void showSuggestJoin(String email);

        void moveToCaptchaActivity();
    }

}
