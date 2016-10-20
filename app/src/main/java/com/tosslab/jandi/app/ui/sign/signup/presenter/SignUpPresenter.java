package com.tosslab.jandi.app.ui.sign.signup.presenter;

/**
 * Created by tee on 16. 5. 25..
 */

public interface SignUpPresenter {

    boolean checkPasswordValidation(String password);

    boolean checkEmailValidation(String email);

    void trySignUp(String name, String email, String password);

    interface View {

        void showProgressWheel();

        void dismissProgressWheel();

        void showErrorInsertEmail();

        void showErrorInvalidEmail();

        void removeErrorEmail();

        void showErrorInsertPassword();

        void removeErrorPassword();

        void showErrorShortPassword();

        void showErrorWeakPassword();

        void showNetworkErrorToast();

        void showErrorDuplicationEmail();

        void startSignUpRequestVerifyActivity();
    }

}
