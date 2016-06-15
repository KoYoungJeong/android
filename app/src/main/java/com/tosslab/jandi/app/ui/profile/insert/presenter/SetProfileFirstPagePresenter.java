package com.tosslab.jandi.app.ui.profile.insert.presenter;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.files.upload.ProfileFileUploadControllerImpl;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.profile.modify.model.ModifyProfileModel;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.io.File;

/**
 * Created by tee on 16. 3. 16..
 */

@EBean
public class SetProfileFirstPagePresenter {

    ModifyProfileModel model;

    @Bean(ProfileFileUploadControllerImpl.class)
    ProfileFileUploadControllerImpl fileUploadController;

    private View view;

    @AfterInject
    void initObject() {
        model = new ModifyProfileModel();
    }

    @Background
    public void requestProfile() {
        view.showProgressWheel();
        try {
            User me;
            if (!NetworkCheckUtil.isConnected()) {
                me = model.getSavedProfile();
            } else {
                me = model.getProfile();
            }
            view.dismissProgressWheel();
            view.setTeamName(TeamInfoLoader.getInstance().getTeamName());
            view.displayProfileImage(me);
            view.displayProfileName(me.getName());
        } catch (Exception e) {
            LogUtil.e("get profile failed", e);
            view.dismissProgressWheel();
            view.showFailProfile();
        }
    }

    @Background
    public void updateProfileName(String name) {
        view.showProgressWheel();
        try {
            ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
            reqUpdateProfile.name = name;
            model.updateProfile(reqUpdateProfile);
            view.displayProfileName(name);
        } catch (RetrofitException e) {
            e.printStackTrace();
            view.updateProfileFailed();
        } finally {
            view.dismissProgressWheel();
        }
    }

    public void startUploadProfileImage(Activity activity, String filePath) {
        fileUploadController.startUpload(activity, null, -1, filePath, null);
    }

    public void onProfileImageChange(User member) {
        if (member != null && model.isMyId(member.getId())) {
            view.displayProfileImage(member);
        }
    }

    public void onRequestCropImage(Fragment fragment) {
        fileUploadController.selectFileSelector(ModifyProfileActivity.REQUEST_CROP, fragment);
    }

    public void onRequestCamera(Fragment fragment) {
        fileUploadController.selectFileSelector(FileUploadController.TYPE_UPLOAD_TAKE_PHOTO, fragment);
    }

    public void onRequestCharacter(Fragment fragment) {
        fileUploadController.selectFileSelector(ModifyProfileActivity.REQUEST_CHARACTER, fragment);
    }

    public File getFilePath() {
        return fileUploadController.getUploadedFile();
    }

    public void setView(View view) {
        this.view = view;
    }

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