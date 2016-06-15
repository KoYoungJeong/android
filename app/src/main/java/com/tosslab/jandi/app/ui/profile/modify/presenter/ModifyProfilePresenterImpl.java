package com.tosslab.jandi.app.ui.profile.modify.presenter;

import android.app.Activity;

import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.files.upload.ProfileFileUploadControllerImpl;
import com.tosslab.jandi.app.local.orm.repositories.info.HumanRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.profile.modify.model.ModifyProfileModel;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.io.File;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


@EBean
public class ModifyProfilePresenterImpl implements ModifyProfilePresenter {

    @Bean(ProfileFileUploadControllerImpl.class)
    ProfileFileUploadControllerImpl filePickerViewModel;

    @Bean
    ModifyProfileModel modifyProfileModel;

    private View view;

    @Override
    public void onRequestProfile() {
        Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(Subscriber<? super User> subscriber) {
                User savedProfile = modifyProfileModel.getSavedProfile();
                subscriber.onNext(savedProfile);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.computation())
                .doOnSubscribe(() -> view.showProgressWheel())
                .doOnUnsubscribe(() -> view.dismissProgressWheel())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                    view.displayProfile(user);
                }, Throwable::printStackTrace);
    }

    @Override
    public void onUpdateProfile(ReqUpdateProfile reqUpdateProfile) {

        Observable.create(new Observable.OnSubscribe<Human>() {
            @Override
            public void call(Subscriber<? super Human> subscriber) {

                try {
                    Human human = modifyProfileModel.updateProfile(reqUpdateProfile);
                    subscriber.onNext(human);
                } catch (RetrofitException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(() -> view.showProgressWheel())
                .doOnUnsubscribe(() -> view.dismissProgressWheel())
                .doOnNext(human1 -> {
                    HumanRepository.getInstance().updateHuman(human1);
                    TeamInfoLoader.getInstance().refresh();
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
        filePickerViewModel.startUpload(activity, null, -1, filePath, null);
    }

    @Override
    public void onProfileChange(User user) {
        if (user != null && modifyProfileModel.isMyId(user.getId())) {
            view.displayProfile(user);
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
