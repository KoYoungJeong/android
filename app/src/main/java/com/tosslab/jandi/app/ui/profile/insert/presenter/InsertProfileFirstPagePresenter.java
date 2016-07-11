package com.tosslab.jandi.app.ui.profile.insert.presenter;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.tosslab.jandi.app.team.member.User;

import java.io.File;

/**
 * Created by tee on 16. 6. 21..
 */
public interface InsertProfileFirstPagePresenter {

    void requestProfile();

    void updateProfileName(String name);

    void startUploadProfileImage(Activity activity, String filePath);

    void onProfileImageChange(User member);

    void onRequestCropImage(Fragment fragment);

    void onRequestCamera(Fragment fragment);

    void onRequestCharacter(Fragment fragment);

    File getFilePath();

    public interface View {
        void showProgressWheel();

        void dismissProgressWheel();

        void displayProfileName(String name);

        void displayProfileImage(User me);

        void showFailProfile();

        void updateProfileFailed();

        void setTeamName(String teamName);
    }
}
