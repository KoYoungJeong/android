package com.tosslab.jandi.app.ui.profile.email.presenter;

/**
 * Created by tee on 2016. 12. 22..
 */

public interface EmailChoosePresenter {

    void requestDeleteEmail(String email);

    void setChangePrimaryEmail();

    void requestNewEmail(String email);

    void onEmailItemSelected(int position);

    void onEmailItemLongClicked(int position);

    interface View {
        void finishWithResultOK();

        void showProgressWheel();

        void dismissProgressWheel();

        void showSuccessToast(int messageResourceId);

        void showFailToast(int messageResourceId);

        void showWarning(int messageResourceId);

        void showDeleteEmail(final String email);

        void refreshListView();

        void showNewEmailDialog();

        void activityFinish();
    }
}
