package com.tosslab.jandi.app.ui.intro.signin.presenter;

/**
 * Created by tee on 16. 5. 25..
 */

public interface MainSignInPresenter {

    interface View {

        void showErrorInsertEmail();

        void showErrorInvalidEmail();

        void showErrorInsertPassword();

        void showErrorInvalidPassword();

        void showErrorInvalidEmailOrPassword();

        void showSignInButtonEnabled();

        void showSignInButtonDisabled();

        void moveToSignUp();

    }

}
