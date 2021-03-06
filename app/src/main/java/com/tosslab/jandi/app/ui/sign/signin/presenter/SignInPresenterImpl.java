package com.tosslab.jandi.app.ui.sign.signin.presenter;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.ui.sign.signin.model.SignInModel;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.SignOutUtil;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrSignIn;
import com.tosslab.jandi.app.utils.parse.PushUtil;

import javax.inject.Inject;

import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tee on 16. 5. 25..
 */

public class SignInPresenterImpl implements SignInPresenter {

    @Inject
    SignInModel model;

    private SignInPresenter.View view;

    @Inject
    public SignInPresenterImpl(SignInPresenter.View view) {
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
        } else if (!model.isValidPassword(password)) {
            view.showErrorInvalidPassword();
            return false;
        }
        view.removeErrorPassword();
        return true;
    }

    @Override
    public void trySignIn(String email, String password, String captchaResponse) {

        final boolean emailValidation = checkEmailValidation(email);
        final boolean passwordValidation = checkPasswordValidation(password);

        if (!(emailValidation && passwordValidation)) {
            return;
        }

        view.showProgressDialog();

        Observable.fromCallable(() -> model.login(email, password, captchaResponse))
                .subscribeOn(Schedulers.io())
                .doOnNext(accessToken -> {
                    SignOutUtil.initSignData();
                    model.updateLoginId(email);
                    model.saveTokenInfo(accessToken);
                    PushUtil.registPush();
                    model.updateAbsenceInfo();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accessToken -> {
                    getAccountInfo(email);
                }, t -> {
                    view.dismissProgressDialog();
                    if (!(t instanceof RetrofitException)) {
                        return;
                    }
                    RetrofitException error = (RetrofitException) t;
                    if (error.getStatusCode() < 500) {
                        try {
                            switch (error.getResponseCode()) {
                                case 40000:
                                case 40021: // 메일이 존재하지 않는 경우
                                case 40007: // 인증된 계정이 아닌 경우(잔디를 처음 사용하는 사용자에 해당)
                                case 40019: // 인증된 계정이 아닌 경우(연결된 다른 계정이 있는 경우)
                                    view.showErrorInvalidEmailOrPassword();
                                    break;
                                case 40100:
                                case 40101:
                                    view.moveToCaptchaActivity();
                                    break;
                            }
                            SprinklrSignIn.sendFailLog(JandiConstants.NetworkError.DATA_NOT_FOUND);
                        } catch (Exception e) {
                            view.showNetworkErrorToast();
                            SprinklrSignIn.sendFailLog(JandiConstants.NetworkError.BAD_REQUEST);
                        }
                    } else if (error.getStatusCode() == 503) {
                        if (error.getResponseCode() == 50302) {
                            // 구글에 문제가 있어 reCaptcha에 대한 검증이 불가능 한 경우
                        }
                    } else {
                        view.showNetworkErrorToast();
                        SprinklrSignIn.sendFailLog(JandiConstants.NetworkError.BAD_REQUEST);
                    }
                });
    }

    private void getAccountInfo(String email) {
        Observable.fromCallable(() -> model.getAccountInfo())
                .subscribeOn(Schedulers.io())
                .doOnNext(accountInfo -> {
                    model.saveAccountInfo(accountInfo);
                    ResAccessToken accessToken = TokenUtil.getTokenObject();
                    model.subscribePush(accessToken.getDeviceId());
                    JandiPreference.setFirstLogin(JandiApplication.getContext());

                    SprinklrSignIn.sendLog(false, false, email);
                    AnalyticsUtil.flushSprinkler();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    view.dismissProgressDialog();
                    view.moveToTeamSelectionActivity(email);
                }, t -> view.showNetworkErrorToast());
    }

    @Override
    public void forgotPassword(String email) {
        Completable.fromCallable(() -> model.requestPasswordReset(email))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> view.showSuccessPasswordResetToast(),
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

}