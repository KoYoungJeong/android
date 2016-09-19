package com.tosslab.jandi.app.ui.sign.signup.presenter;

import com.google.ads.conversiontracking.AdWordsConversionReporter;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.ui.sign.signup.model.SignUpModel;
import com.tosslab.jandi.app.utils.LanguageUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrVerificationMail;

import javax.inject.Inject;

import rx.Observable;
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
    public boolean checkNameValidation(String name) {
        if (model.isEmptyName(name)) {
            view.showErrorInsertName();
            return false;
        }
        view.removeErrorName();
        return true;
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

        boolean check = checkNameValidation(name);
        check = checkEmailValidation(email) && check;
        check = checkPasswordValidation(password) && check;

        if (!check) {
            return;
        }

        String lang = LanguageUtil.getLanguage();

        view.showProgressWheel();

        Observable.create(subscriber -> {
            try {
                model.requestSignUp(email, password, name, lang);
                AdWordsConversionReporter.reportWithConversionId(JandiApplication.getContext(),
                        "957512006", "fVnsCMKD_GEQxvLJyAM", "0.00", true);
                SprinklrVerificationMail.sendLog(email);
                subscriber.onNext(new Object());
            } catch (RetrofitException e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
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
