package com.tosslab.jandi.app.ui.sign.signup.presenter;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.ui.sign.signup.model.SignUpModel;
import com.tosslab.jandi.app.utils.LanguageUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrVerificationMail;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SignUpPresenterImpl implements SignUpPresenter {
    @Inject
    SignUpModel model;

    private SignUpPresenter.View view;

    private List<String> emailTypos;

    @Inject
    public SignUpPresenterImpl(SignUpPresenter.View view) {
        this.view = view;
        emailTypos = new ArrayList<>();
    }

    @Override
    public boolean checkEmailValidation(String email) {
        if (model.isEmptyEmail(email)) {
            view.showErrorInsertEmail();
            return false;
        } else if (!model.isValidEmailFormat(email)) {
            view.showErrorInvalidEmail();
            return false;
        } else {
            int idx = email.indexOf("@");
            if (idx >= 0) {
                String domain = email.substring(idx + 1);
                for (String emailTypo : emailTypos) {
                    if (TextUtils.equals(emailTypo, domain)) {
                        view.showErrorEmailTypo();
                        return false;
                    }
                }
            }
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
            String lang = LanguageUtil.getLanguage();
            model.requestSignUp(email, password, name, lang);
            AnalyticsUtil.sendConversion("Android_Account mail send", "957512006", "fVnsCMKD_GEQxvLJyAM");
            SprinklrVerificationMail.sendLog(email);
            return Completable.complete();
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

    @Override
    public void onInitEmailTypo() {
        Observable.fromCallable(() -> model.getEmailTypo()).subscribeOn(Schedulers.io())
                .filter(emailTypo -> emailTypo.getDomains() != null && !emailTypo.getDomains().isEmpty())
                .subscribe(emailTypo -> emailTypos.addAll(emailTypo.getDomains()));
    }

}
