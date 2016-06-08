package com.tosslab.jandi.app.ui.sign.signin.presenter;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.mixpanel.MixpanelAccountAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.sign.signin.model.SignInModel;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.parse.PushUtil;

import javax.inject.Inject;

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
    public void trySignIn(String email, String password) {

        final boolean emailValidation = checkEmailValidation(email);
        final boolean passwordValidation = checkPasswordValidation(password);

        if (!(emailValidation && passwordValidation)) {
            return;
        }

        view.showProgressDialog();

        Observable.<ResAccessToken>create(subscriber -> {
            try {
                ResAccessToken accessToken = model.login(email, password);
                subscriber.onNext(accessToken);
            } catch (RetrofitException e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io())
                .doOnNext(accessToken -> {
                    model.saveTokenInfo(accessToken);
                    PushUtil.registPush();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accessToken -> {
                    view.dismissProgressDialog();
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
                            }
                            model.trackSignInFail(JandiConstants.NetworkError.DATA_NOT_FOUND);
                        } catch (Exception e) {
                            view.showNetworkErrorToast();
                            model.trackSignInFail(JandiConstants.NetworkError.BAD_REQUEST);
                        }
                    } else {
                        view.showNetworkErrorToast();
                        model.trackSignInFail(JandiConstants.NetworkError.BAD_REQUEST);
                    }
                });
    }

    private void getAccountInfo(String email) {
        Observable.<ResAccountInfo>create(subscriber -> {
            try {
                ResAccountInfo accountInfo = model.getAccountInfo();
                subscriber.onNext(accountInfo);
            } catch (RetrofitException e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io())
                .doOnNext(accountInfo -> {
                    model.saveAccountInfo(accountInfo);
                    ResAccessToken accessToken = TokenUtil.getTokenObject();
                    model.subscribePush(accessToken.getDeviceId());
                    JandiPreference.setFirstLogin(JandiApplication.getContext());

                    MixpanelAccountAnalyticsClient
                            .getInstance(JandiApplication.getContext(), accountInfo.getId())
                            .trackAccountSingingIn();

                    model.trackSignInSuccess();
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    view.dismissProgressDialog();
                    view.moveToTeamSelectionActivity(email);
                }, t -> view.showNetworkErrorToast());
    }

    @Override
    public void forgotPassword(String email) {
        Observable.<Boolean>create(subscriber -> {
            try {
                model.requestPasswordReset(email);
                subscriber.onNext(true);
            } catch (Exception e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> view.showSuccessPasswordResetToast(),
                        e -> view.showFailPasswordResetToast());
    }

}