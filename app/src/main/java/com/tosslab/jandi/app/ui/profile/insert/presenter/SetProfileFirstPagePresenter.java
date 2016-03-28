package com.tosslab.jandi.app.ui.profile.insert.presenter;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.files.upload.ProfileFileUploadControllerImpl;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.profile.modify.model.ModifyProfileModel;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.io.File;

import retrofit.RetrofitError;

/**
 * Created by tee on 16. 3. 16..
 */

@EBean
public class SetProfileFirstPagePresenter {

    @Bean
    ModifyProfileModel model;

    @Bean(ProfileFileUploadControllerImpl.class)
    ProfileFileUploadControllerImpl fileUploadController;

    private View view;

    @Background
    public void requestProfile() {
        view.showProgressWheel();
        try {
            ResLeftSideMenu.User me;
            if (!NetworkCheckUtil.isConnected()) {
                me = model.getSavedProfile();
            } else {
                me = model.getProfile();
            }
            view.dismissProgressWheel();
            view.setTeamName(EntityManager.getInstance().getTeamName());
            view.displayProfileImage(me);
            view.displayProfileName(me.name);
        } catch (RetrofitError e) {
            LogUtil.e("get profile failed", e);
            view.dismissProgressWheel();
            view.showFailProfile();
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
            model.updateProfileName(new ReqProfileName(name));
            view.displayProfileName(name);
        } catch (RetrofitError e) {
            e.printStackTrace();
            view.updateProfileFailed();
        } finally {
            view.dismissProgressWheel();
        }
    }

    public void startUploadProfileImage(Activity activity, String filePath) {
        fileUploadController.startUpload(activity, null, -1, filePath, null);
    }

    public void onProfileImageChange(ResLeftSideMenu.User member) {
        if (member != null && model.isMyId(member.id)) {
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

        void displayProfileImage(ResLeftSideMenu.User me);

        void showFailProfile();

        void updateProfileFailed();

        void setTeamName(String teamName);
    }

}