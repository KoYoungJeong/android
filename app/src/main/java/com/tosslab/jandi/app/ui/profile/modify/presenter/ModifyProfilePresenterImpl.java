package com.tosslab.jandi.app.ui.profile.modify.presenter;

import android.app.Activity;

import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.files.upload.ProfileFileUploadControllerImpl;
import com.tosslab.jandi.app.local.orm.repositories.info.HumanRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.profile.modify.model.ModifyProfileModel;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.io.File;


@EBean
public class ModifyProfilePresenterImpl implements ModifyProfilePresenter {

    @Bean(ProfileFileUploadControllerImpl.class)
    ProfileFileUploadControllerImpl filePickerViewModel;

    @Bean
    ModifyProfileModel modifyProfileModel;

    private View view;

    @Override
    @Background
    public void onRequestProfile() {
        view.showProgressWheel();
        try {
            User me;
            if (!NetworkCheckUtil.isConnected()) {
                me = modifyProfileModel.getSavedProfile();
            } else {
                me = modifyProfileModel.getProfile();
            }
            view.dismissProgressWheel();
            view.displayProfile(me);
        } catch (Exception e) {
            LogUtil.e("get profile failed", e);
            view.dismissProgressWheel();
            view.showFailProfile();
        }
    }

    @Background
    @Override
    public void onUpdateProfileExtraInfo(ReqUpdateProfile reqUpdateProfile) {
        view.showProgressWheel();
        try {
            modifyProfileModel.updateProfile(reqUpdateProfile);
            HumanRepository.getInstance().updateProfile(TeamInfoLoader.getInstance().getMyId(),
                    reqUpdateProfile.department,
                    reqUpdateProfile.phoneNumber,
                    reqUpdateProfile.position,
                    reqUpdateProfile.statusMessage);

            TeamInfoLoader.getInstance().refresh();
            view.updateProfileSucceed();
            view.displayProfile(null);
        } catch (RetrofitException e) {
            e.printStackTrace();
            LogUtil.e("get profile failed", e);
            view.updateProfileFailed();
        } finally {
            view.dismissProgressWheel();
        }
    }

    @Background
    @Override
    public void updateProfileName(String name) {
        view.showProgressWheel();
        try {
            modifyProfileModel.updateProfileName(new ReqProfileName(name));
            HumanRepository.getInstance().updateName(TeamInfoLoader.getInstance().getMyId(), name);
            TeamInfoLoader.getInstance().refresh();
            view.updateProfileSucceed();
            view.successUpdateNameColor();
        } catch (RetrofitException e) {
            e.printStackTrace();
            view.updateProfileFailed();
        } finally {
            view.dismissProgressWheel();
        }
    }

    @Background
    @Override
    public void onUploadEmail(String email) {

        if (!NetworkCheckUtil.isConnected()) {
            view.showCheckNetworkDialog();
            return;
        }

        try {
            modifyProfileModel.updateProfileEmail(email);
            HumanRepository.getInstance().updateEmail(TeamInfoLoader.getInstance().getMyId(), email);
            TeamInfoLoader.getInstance().refresh();
            view.updateProfileSucceed();
            view.successUpdateEmailColor();
        } catch (RetrofitException e) {
            view.updateProfileFailed();
        }
    }

    @Override
    public void onStartUpload(Activity activity, String filePath) {
        filePickerViewModel.startUpload(activity, null, -1, filePath, null);
    }

    @Override
    public void onProfileChange(User member) {
        if (member != null && modifyProfileModel.isMyId(member.getId())) {
            view.displayProfile(member);
            view.closeDialogFragment();
        }
    }

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void onEditEmailClick(String email) {
        String[] accountEmails = modifyProfileModel.getAccountEmails();
        view.showEmailChooseDialog(accountEmails, email);
    }

    @Override
    public void onRequestCropImage(Activity activity) {
        filePickerViewModel.selectFileSelector(ModifyProfileActivity.REQUEST_CROP, activity);
    }

    @Override
    public void onRequestCamera(Activity activity) {
        filePickerViewModel.selectFileSelector(FileUploadController.TYPE_UPLOAD_TAKE_PHOTO, activity);
    }

    @Override
    public void onRequestCharacter(Activity activity) {
        filePickerViewModel.selectFileSelector(ModifyProfileActivity.REQUEST_CHARACTER, activity);
    }

    @Override
    public File getFilePath() {
        return filePickerViewModel.getUploadedFile();
    }
}
