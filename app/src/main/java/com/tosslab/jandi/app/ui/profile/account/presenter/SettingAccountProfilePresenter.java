package com.tosslab.jandi.app.ui.profile.account.presenter;

/**
 * Created by tee on 2016. 9. 30..
 */

public interface SettingAccountProfilePresenter {

    void onSignOutAction();

    void onEmailChoose();

    void setAccountName();

    void setPrimaryEmail();

    void updatePrimaryEmail(String email);

    interface View {

        void dismissProgressWheel();

        void showProgressWheel();

        void showSuccessToast(String message);

        void moveLoginActivity();

        void showEmailChooseDialog(String[] emails);

        void setName(String name);

        void setPrimaryEmail(String email);
    }
}
