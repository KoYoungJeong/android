package com.tosslab.jandi.app.ui.invites.emails.presenter;

/**
 * Created by tee on 2016. 12. 12..
 */

public interface InviteEmailPresenter {

    void addEmail(String email, int mode);

    void setStatusByEmailValid(String email);

    void onInvitedUsersChanged();

    void startInvitationForAssociate(long selectedTopicId);

    void startInvitation();

    void onTopicSelected();

    interface View {
        void enableAddButton(boolean enable);

        void changeContentInvitationButton(int cnt);

        void setErrorInputSelectedEmail();

        void setErrorSelectedTopic();

        void showProgressWheel();

        void dismissProgressWheel();

        void showUnkownFailedDialog();

        void showPartiallyFailedDialog();

        void showSuccessDialog();

        void changeInvitationButtonIfPatiallyFailed();

        void showDialogOver10();

        void showErrorExceedFreeMembersDialog();
    }
}
