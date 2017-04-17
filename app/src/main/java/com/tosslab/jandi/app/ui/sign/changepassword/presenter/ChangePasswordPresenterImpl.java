package com.tosslab.jandi.app.ui.sign.changepassword.presenter;

import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.ui.sign.changepassword.model.ChangePasswordModel;

import javax.inject.Inject;

import rx.Completable;
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
    public boolean checkNewPasswordValidation(String password) {
        if (!model.isEmptyPassword(password) &&
                (!model.isValidLengthPassword(password) ||
                        !model.isValidCharacterPassword(password))) {
            view.showErrorWeakNewPassword();
            return false;
        }
        view.removeErrorNewPassword();
        return true;
    }

    @Override
    public boolean checkNewPasswordAgainValidation(String newPassword, String newAgainPassword) {
        if (!newPassword.isEmpty() &&
                !newAgainPassword.isEmpty() &&
                !newPassword.equals(newAgainPassword)) {
            view.showErrorNotSameNewPassword();
            return false;
        }
        view.removeErrorNewPassword();
        return true;
    }

    @Override
    public void setNewPassword(String oldPassword, String newPassword, String newPasswordAgain) {
        if (checkNewPasswordAgainValidation(newPassword, newPasswordAgain)) {
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
                            view.setDoneButtonEnable(false);
                        }
                        view.dismissProgressWheel();
                    });
        } else {
            view.setDoneButtonEnable(false);
        }
    }

    @Override
    public void forgotPassword(String email) {
        Completable.fromCallable(() -> model.requestPasswordReset(email))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> view.showPasswordResetEmailSendSucsess(),
                        e -> {
                            if (e instanceof RetrofitException) {
                                RetrofitException error = (RetrofitException) e;
                                if (error.getResponseCode() == 40000
                                        || error.getResponseCode() == 40022) {
                                    view.showSuggestJoin(email);
                                } else {
                                    view.showNetworkErrorToast();
                                }
                            } else {
                                view.showFailPasswordResetToast();
                            }
                        });
    }


    @Override
    public void checkCanSendChangePassword(String oldPassword, String newPassword, String newPasswordAgain) {
        if (oldPassword.length() > 0
                && newPassword.length() > 0
                && newPasswordAgain.length() > 0
                && model.isValidCharacterPassword(newPassword)) {
            view.setDoneButtonEnable(true);
        } else {
            view.setDoneButtonEnable(false);
        }
    }

}
