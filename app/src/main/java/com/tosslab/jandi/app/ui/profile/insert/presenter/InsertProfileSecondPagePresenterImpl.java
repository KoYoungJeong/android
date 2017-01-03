package com.tosslab.jandi.app.ui.profile.insert.presenter;

import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.local.orm.repositories.info.HumanRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.profile.modify.model.ModifyProfileModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.Observable;
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
        Observable.fromCallable(() -> modifyProfileModel.getSavedProfile()).subscribeOn(Schedulers.io())
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
        Observable.defer(() -> Observable.just(modifyProfileModel.getAccountEmails()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(accountEmails -> view.showEmailChooseDialog(accountEmails, email))
                .subscribe();
    }

    @Override
    public void setEmail(String email) {
        Observable.defer(() -> Observable.just(modifyProfileModel.getAccountEmails()))
                .subscribeOn(Schedulers.io())
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

        view.showProgressWheel();

        Completable.fromCallable(() -> {
            ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
            reqUpdateProfile.department = department;
            reqUpdateProfile.position = position;
            reqUpdateProfile.phoneNumber = phoneNumber;
            reqUpdateProfile.statusMessage = statusMessage;
            Human human = modifyProfileModel.updateProfile(reqUpdateProfile);
            if (human != null && human.getProfile() != null) {
                human.getProfile().setId(human.getId());
            }
            HumanRepository.getInstance().updateHuman(human);
            TeamInfoLoader.getInstance().refresh();
            EventBus.getDefault().post(new ProfileChangeEvent(human));
            return human;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.dismissProgressWheel();
                    view.finish();
                }, t -> {
                    LogUtil.e("get profile failed", t);
                    view.updateProfileFailed();
                });
    }

}
