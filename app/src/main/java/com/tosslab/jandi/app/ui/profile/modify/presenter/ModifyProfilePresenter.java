package com.tosslab.jandi.app.ui.profile.modify.presenter;

import android.app.Activity;

import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.team.member.User;

import java.io.File;

public interface ModifyProfilePresenter {

    void onEditEmailClick();

    void onRequestCropImage(Activity activity);

    void onRequestProfile();

    void onUpdateProfile(ReqUpdateProfile reqUpdateProfile);

    void onStartUpload(Activity activity, String filePath);

    void onProfileChange(User member);

    void onRequestCamera(Activity activity);

    void onRequestCharacter(Activity activity);

    File getFilePath();

    interface View {

        void showProgressWheel();

        void displayProfile(User me);

        void dismissProgressWheel();

        void showEmailChooseDialog(String[] accountEmails, String email);

        void showCheckNetworkDialog();

        void updateProfileSucceed();

        void updateProfileFailed();

        void closeDialogFragment();
    }
}
