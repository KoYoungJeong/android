package com.tosslab.jandi.app.ui.sign.signup.presenter;

import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.ui.sign.signup.model.SignUpModel;
import com.tosslab.jandi.app.utils.LanguageUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrVerificationMail;

import javax.inject.Inject;

import rx.Completable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tee on 16. 5. 25..
 */
public class SignUpPresenterImpl implements SignUpPresenter {
    @Inject
    SignUpModel model;

    private SignUpPresenter.View view;

    @Inject
    public SignUpPresenterImpl(SignUpPresenter.View view) {
        this.view = view;
    }

    @Override
    public boolean checkEmailValidation(String email) {
        if (model.isEmptyEmail(email)) {
            view.showErrorInsertEmail();
            return false;
        } else if (!model.isValidEmailFormat(email)) {
            view.showErrorInvalidEmail();
            return false;
        }
        view.removeErrorEmail();
        return true;
    }

    @Override
    public boolean checkPasswordValidation(String password) {
        if (model.isEmptyPassword(password)) {
            view.showErrorInsertPassword();
            return false;
        } else if (!model.isValidLengthPassword(password)) {
            view.showErrorShortPassword();
            return false;
        } else if (!model.isValidCharacterPassword(password)) {
            view.showErrorWeakPassword();
            return false;
        }
        view.removeErrorPassword();
        return true;
    }

    @Override
    public void trySignUp(String name, String email, String password) {

        boolean check = checkEmailValidation(email);
        check = checkPasswordValidation(password) && check;

        if (!check) {
            return;
        }


        view.showProgressWheel();


        Completable.fromCallable(() -> {
            try {
                String lang = LanguageUtil.getLanguage();
                model.requestSignUp(email, password, name, lang);
                AnalyticsUtil.sendConversion("Android_Account mail send", "957512006", "fVnsCMKD_GEQxvLJyAM");
                SprinklrVerificationMail.sendLog(email);
                return Completable.complete();
            } catch (RetrofitException e) {
                throw e;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.dismissProgressWheel();
                    view.startSignUpRequestVerifyActivity();
                }, e -> {
                    view.dismissProgressWheel();
                    if (!(e instanceof RetrofitException)) {
                        return;
                    }
                    RetrofitException exception = (RetrofitException) e;
                    if (exception.getResponseCode() == 40001) {
                        view.showErrorDuplicationEmail();
                    } else {
                        view.showNetworkErrorToast();
                    }
                    SprinklrVerificationMail.sendFailLog(((RetrofitException) e).getResponseCode());
                });
    }

}
