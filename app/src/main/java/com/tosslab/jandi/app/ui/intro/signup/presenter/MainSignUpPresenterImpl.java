package com.tosslab.jandi.app.ui.intro.signup.presenter;

import com.google.ads.conversiontracking.AdWordsConversionReporter;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.ui.intro.signup.model.MainSignUpModel;
import com.tosslab.jandi.app.utils.LanguageUtil;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tee on 16. 5. 25..
 */
public class MainSignUpPresenterImpl implements MainSignUpPresenter {
    @Inject
    MainSignUpModel model;

    private MainSignUpPresenter.View view;

    @Inject
    public MainSignUpPresenterImpl(MainSignUpPresenter.View view) {
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

        if (!(checkNameValidation(name) &&
                checkEmailValidation(email) &&
                checkPasswordValidation(password))) {
            return;
        }

        String lang = LanguageUtil.getLanguage();

        view.showProgressWheel();

        Observable.create((Subscriber<? super Object> subscriber) -> {
            try {
                model.requestSignUp(email, password, name, lang);
                model.trackSendEmailSuccess(email);
                AdWordsConversionReporter.reportWithConversionId(JandiApplication.getContext(),
                        "957512006", "fVnsCMKD_GEQxvLJyAM", "0.00", true);
                subscriber.onCompleted();
            } catch (RetrofitException e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
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
                    model.trackSendEmailFail(exception.getResponseCode());
                }, () -> {
                    view.startSignUpRequestVerifyActivity();
                    view.dismissProgressWheel();
                });
    }

}
