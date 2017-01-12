package com.tosslab.jandi.app.ui.profile.modify.presenter;

import android.app.Activity;

import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.files.upload.ProfileFileUploadControllerImpl;
import com.tosslab.jandi.app.local.orm.repositories.info.HumanRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.profile.modify.model.ModifyProfileModel;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.io.File;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class ModifyProfilePresenterImpl implements ModifyProfilePresenter {


    ProfileFileUploadControllerImpl filePickerViewModel;

    ModifyProfileModel modifyProfileModel;

    private View view;

    private long memberId = -1;

    @Inject
    public ModifyProfilePresenterImpl(ProfileFileUploadControllerImpl filePickerViewModel,
                                      ModifyProfileModel modifyProfileModel, View view) {
        this.filePickerViewModel = filePickerViewModel;
        this.modifyProfileModel = modifyProfileModel;
        this.view = view;
    }

    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }

    @Override
    public void onRequestProfile() {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> view.showProgressWheel());

        Observable.defer(() -> Observable.just(modifyProfileModel.getSavedProfile(memberId)))
                .doOnUnsubscribe(() -> view.dismissProgressWheel())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                    view.displayProfile(user);
                }, Throwable::printStackTrace);
    }

    @Override
    public void onUpdateProfile(ReqUpdateProfile reqUpdateProfile) {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> view.showProgressWheel());

        Observable.defer(() -> {
            try {
                Human human = modifyProfileModel.updateProfile(reqUpdateProfile, memberId);
                return Observable.just(human);
            } catch (RetrofitException e) {
                e.printStackTrace();
                return Observable.error(e);
            }
        }).subscribeOn(Schedulers.io())
                .doOnUnsubscribe(() -> view.dismissProgressWheel())
                .doOnNext(human1 -> {
                    HumanRepository.getInstance().updateHuman(human1);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(human2 -> {
                    view.updateProfileSucceed();
                    view.displayProfile(new User(human2));
                }, throwable -> {
                    LogUtil.e("get profile failed", throwable);
                    view.updateProfileFailed();
                });
    }

    @Override
    public void onStartUpload(Activity activity, String filePath) {
        filePickerViewModel.startUpload(activity, null, memberId, filePath, null);
    }

    @Override
    public void onProfileChange(User user) {
        if (user != null) {
            Observable.just(new Object())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(o -> {
                        view.displayProfile(user);
                        view.closeDialogFragment();
                    });
        }
    }

    @Override
    public void onEditEmailClick() {
        User savedProfile = modifyProfileModel.getSavedProfile(memberId);
        String[] accountEmails = modifyProfileModel.getAccountEmails();
        view.showEmailChooseDialog(accountEmails, savedProfile.getEmail());
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
