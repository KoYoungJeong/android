package com.tosslab.jandi.app.ui.settings.main.presenter;

public interface SettingsPresenter {

    void setView(View view);

    void onSignOut();

    void startSignOut();

    void onSetUpOrientation(String selectedValue);

    void onSetUpVersion();

    void onInitViews();

    interface View {

        void showSignoutDialog();

        void showCheckNetworkDialog();

        void showSuccessToast(String message);

        void showProgressDialog();

        void dismissProgressDialog();

        void setOrientationViewVisibility(boolean show);

        void setOrientation(int orientation);

        void setOrientationSummary(String value);

        void moveLoginActivity();

        void setVersion(String version);
    }
}
