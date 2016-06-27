package com.tosslab.jandi.app.ui.profile.insert.presenter;

import com.tosslab.jandi.app.team.member.User;

/**
 * Created by tee on 16. 6. 21..
 */
public interface InsertProfileSecondPagePresenter {

    void requestProfile();

    void chooseEmail(String email);

    void setEmail(String email);

    void uploadEmail(String email);

    void uploadExtraInfo(
            String department, String position, String phoneNumber, String statusMessage);

    public interface View {
        void showEmailChooseDialog(String[] accountEmails, String email);

        void showProgressWheel();

        void dismissProgressWheel();

        void showFailProfile();

        void displayProfileInfos(User me);

        void setEmail(String[] accountEmails, String email);

        void showCheckNetworkDialog();

        void updateProfileFailed();

        void updateProfileSucceed();

        void finish();
    }
}
