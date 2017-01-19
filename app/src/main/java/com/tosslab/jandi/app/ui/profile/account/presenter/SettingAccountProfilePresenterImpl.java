package com.tosslab.jandi.app.ui.profile.account.presenter;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.ui.profile.account.model.SettingAccountProfileModel;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.SignOutUtil;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tee on 2016. 9. 30..
 */

public class SettingAccountProfilePresenterImpl implements SettingAccountProfilePresenter {

    SettingAccountProfileModel model;

    private SettingAccountProfilePresenter.View view;

    @Inject
    public SettingAccountProfilePresenterImpl(View view, SettingAccountProfileModel model) {
        this.view = view;
        this.model = model;
    }

    @Override
    public void onEmailChoose() {
        String[] accountEmails = model.getAccountEmails();
        if (accountEmails.length > 0) {
            view.showEmailChooseDialog(accountEmails);
        }
    }

    @Override
    public void setAccountName() {
        view.setName(model.getName());
    }

    @Override
    public void updatePrimaryEmail(String email) {
        Observable.defer(() -> {
            model.updateProfileEmail(email);
            return Observable.just(0);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(i -> {
                    view.setPrimaryEmail(email);
                });
    }

    @Override
    public void setPrimaryEmail() {
        view.setPrimaryEmail(model.getPrimaryEmail());
    }

    @Override
    public void onSignOutAction() {
        view.showProgressWheel();

        model.getSignOutObservable()
                .onErrorReturn(throwable -> new ResCommon())
                .doOnNext(resCommon1 -> {
                    SignOutUtil.removeSignData();
                    BadgeUtils.clearBadge(JandiApplication.getContext());
                    JandiSocketService.stopService(JandiApplication.getContext());
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    String toastMessage = JandiApplication.getContext().getString(R.string.jandi_message_logout);
                    view.showSuccessToast(toastMessage);
                    view.dismissProgressWheel();
                    view.moveLoginActivity();
                }, t -> {
                    view.dismissProgressWheel();
                });
    }


}
