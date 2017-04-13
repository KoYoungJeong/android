package com.tosslab.jandi.app.ui.sign.changepassword.presenter;

import com.tosslab.jandi.app.ui.sign.changepassword.model.ChangePasswordModel;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tee on 2017. 4. 11..
 */

public class ChangePasswordPresenterImpl implements ChangePasswordPresenter {

    @Inject
    ChangePasswordModel model;

    @Inject
    ChangePasswordPresenter.View view;

    @Inject
    public ChangePasswordPresenterImpl() {
    }

    @Override
    public boolean checkCurrentPasswordValidation(String password) {
        if (model.isEmptyPassword(password)
                || !model.isValidLengthPassword(password)
                || !model.isValidCharacterPassword(password)) {
            return false;
        }
        view.removeErrorNewPassword();
        return true;
    }

    @Override
    public boolean checkNewPasswordValidation(String password) {
        if (model.isEmptyPassword(password)
                || !model.isValidLengthPassword(password)
                || !model.isValidCharacterPassword(password)) {
            view.showErrorWeakNewPassword();
            return false;
        }
        view.removeErrorNewPassword();
        return true;
    }

    @Override
    public boolean checkNewPasswordAgainValidation(String newPassword, String newAgainPassword) {
        if (!newPassword.equals(newAgainPassword)) {
            view.showErrorNotSameNewPassword();
            return false;
        }
        view.removeErrorNewPassword();
        return true;
    }

    @Override
    public void setNewPassword(String oldPassword, String newPassword) {
        view.showProgressWheel();
        Observable.defer(() ->
                Observable.just(model.changePasswordApi(oldPassword, newPassword)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isOk -> {
                    if (isOk) {
                        view.showSuccessDialog();
                    } else {
                        view.showErrorNotValidCurrentPassword();
                    }
                    view.dismissProgressWheel();
                });
    }

    @Override
    public void checkCanSendChangePassword(String oldPassword, String newPassword, String newPasswordAgain) {
        if (oldPassword.length() > 0
                && newPassword.length() > 0
                && newPasswordAgain.length() > 0
                && newPassword.equals(newPasswordAgain)
                && model.isValidCharacterPassword(newPassword)) {
            view.setDoneButtonEnable(true);
        } else {
            view.setDoneButtonEnable(false);
        }
    }

}
