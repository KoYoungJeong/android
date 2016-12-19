package com.tosslab.jandi.app.ui.profile.insert.presenter;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.profile.modify.model.ModifyProfileModel;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tee on 16. 3. 16..
 */

public class InsertProfileFirstPagePresenterImpl implements InsertProfileFirstPagePresenter {

    private ModifyProfileModel model;

    private FileUploadController fileUploadController;

    private View view;

    @Inject
    public InsertProfileFirstPagePresenterImpl(
            InsertProfileFirstPagePresenter.View view,
            ModifyProfileModel model,
            FileUploadController fileUploadController) {
        this.view = view;
        this.model = model;
        this.fileUploadController = fileUploadController;
    }

    @Override
    public void requestProfile() {
        view.showProgressWheel();
        Observable.fromCallable(() -> model.getSavedProfile())
                .subscribeOn(Schedulers.io())
                .doOnUnsubscribe(() -> view.dismissProgressWheel())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(me -> {
                    view.setTeamName(TeamInfoLoader.getInstance().getTeamName());
                    view.displayProfileImage(me);
                    view.displayProfileName(me.getName());
                })
                .subscribe(o -> {
                }, e -> {
                    LogUtil.e("get profile failed", e);
                    view.showFailProfile();
                });
    }

    @Override
    public void updateProfileName(String name) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
                    reqUpdateProfile.name = name;
                    model.updateProfile(reqUpdateProfile);
                    subscriber.onNext(name);
                } catch (RetrofitException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .doOnSubscribe(() -> view.showProgressWheel())
                .doOnUnsubscribe(() -> view.dismissProgressWheel())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(name1 -> view.displayProfileName(name1))
                .subscribe(o -> {
                }, e -> {
                    e.printStackTrace();
                    view.updateProfileFailed();
                });
    }

    @Override
    public void startUploadProfileImage(Activity activity, String filePath) {
        Observable.just(1)
                .observeOn(Schedulers.io())
                .delay(300, TimeUnit.MILLISECONDS)
                .subscribe(i -> {
                    fileUploadController.startUpload(activity, null, -1, filePath, null);
                });
    }

    @Override
    public void onProfileImageChange(User member) {
        if (member != null && model.isMyId(member.getId())) {
            view.displayProfileImage(member);
        }
    }

    @Override
    public void onRequestCropImage(Fragment fragment) {
        Observable.just(1)
                .observeOn(Schedulers.io())
                .subscribe(i -> {
                    fileUploadController.selectFileSelector(ModifyProfileActivity.REQUEST_CROP, fragment);
                });
    }

    @Override
    public void onRequestCamera(Fragment fragment) {
        Observable.just(1)
                .observeOn(Schedulers.io())
                .subscribe(i -> {
                    fileUploadController.selectFileSelector(FileUploadController.TYPE_UPLOAD_TAKE_PHOTO, fragment);
                });
    }

    @Override
    public void onRequestCharacter(Fragment fragment) {
        Observable.just(1)
                .observeOn(Schedulers.io())
                .subscribe(i -> {
                    fileUploadController.selectFileSelector(ModifyProfileActivity.REQUEST_CHARACTER, fragment);
                });
    }

    @Override
    public File getFilePath() {
        return fileUploadController.getUploadedFile();
    }

}