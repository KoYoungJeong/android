package com.tosslab.jandi.app.ui.profile.insert.presenter;

import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.profile.modify.model.ModifyProfileModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tee on 16. 3. 16..
 */

public class InsertProfileSecondPagePresenterImpl implements InsertProfileSecondPagePresenter {

    private ModifyProfileModel modifyProfileModel;

    private InsertProfileSecondPagePresenter.View view;

    @Inject
    public InsertProfileSecondPagePresenterImpl(ModifyProfileModel modifyProfileModel, View view) {
        this.modifyProfileModel = modifyProfileModel;
        this.view = view;
    }

    @Override
    public void requestProfile() {
        Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(Subscriber<? super User> subscriber) {
                try {
                    User me = modifyProfileModel.getSavedProfile();
                    subscriber.onNext(me);
                } catch (Exception e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(me -> view.displayProfileInfos(me))
                .subscribe(o -> {
                }, e -> {
                    LogUtil.e("get profile failed", e);
                    view.showFailProfile();
                });
    }

    @Override
    public void chooseEmail(String email) {
        Observable.create(new Observable.OnSubscribe<String[]>() {
            @Override
            public void call(Subscriber<? super String[]> subscriber) {
                String[] accountEmails = modifyProfileModel.getAccountEmails();
                subscriber.onNext(accountEmails);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(accountEmails -> view.showEmailChooseDialog(accountEmails, email))
                .subscribe();
    }

    @Override
    public void setEmail(String email) {
        Observable.create(new Observable.OnSubscribe<String[]>() {
            @Override
            public void call(Subscriber<? super String[]> subscriber) {
                String[] accountEmails = modifyProfileModel.getAccountEmails();
                subscriber.onNext(accountEmails);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(accountEmails -> view.setEmail(accountEmails, email))
                .subscribe();
    }

    @Override
    public void uploadEmail(String email) {
        if (!NetworkCheckUtil.isConnected()) {
            view.showCheckNetworkDialog();
            return;
        }

        Observable.create(subscriber -> {
            try {
                modifyProfileModel.updateProfileEmail(email);
            } catch (RetrofitException e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                }, e -> {
                    view.updateProfileFailed();
                });
    }

    @Override
    public void uploadExtraInfo(
            String department, String position, String phoneNumber, String statusMessage) {

        Observable.create(subscriber -> {
            ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
            reqUpdateProfile.department = department;
            reqUpdateProfile.position = position;
            reqUpdateProfile.phoneNumber = phoneNumber;
            reqUpdateProfile.statusMessage = statusMessage;
            try {
                modifyProfileModel.updateProfile(reqUpdateProfile);
                subscriber.onNext(new Object());
            } catch (RetrofitException e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io())
                .doOnSubscribe(() -> view.showProgressWheel())
                .doOnUnsubscribe(() -> {
                    view.dismissProgressWheel();
                    view.finish();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(o -> view.updateProfileSucceed())
                .subscribe(o -> {
                }, e -> {
                    LogUtil.e("get profile failed", e);
                    view.updateProfileFailed();
                });
    }

}
