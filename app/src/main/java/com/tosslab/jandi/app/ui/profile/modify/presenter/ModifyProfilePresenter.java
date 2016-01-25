package com.tosslab.jandi.app.ui.profile.modify.presenter;

import android.app.Activity;

import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import java.io.File;

public interface ModifyProfilePresenter {

    void setView(View view);

    void onEditEmailClick(String email);

    void onRequestCropImage(Activity activity);

    void onRequestProfile();

    void onUpdateProfileExtraInfo(ReqUpdateProfile reqUpdateProfile);

    void updateProfileName(String name);

    void onUploadEmail(String email);

    void onStartUpload(Activity activity, String filePath);

    void onProfileChange(ResLeftSideMenu.User member);

    void onRequestCamera(Activity activity);

    void onRequestCharacter(Activity activity);

    File getFilePath();

    interface View {

        void showProgressWheel();

        void displayProfile(ResLeftSideMenu.User me);

        void showFailProfile();

        void dismissProgressWheel();

        void showEmailChooseDialog(String[] accountEmails, String email);

        void showCheckNetworkDialog();

        void updateProfileSucceed();

        void successUpdateEmailColor();

        void updateProfileFailed();

        void successUpdateNameColor();

        void closeDialogFragment();
    }
}
