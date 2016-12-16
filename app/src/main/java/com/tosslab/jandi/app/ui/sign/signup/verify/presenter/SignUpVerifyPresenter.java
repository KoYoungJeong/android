package com.tosslab.jandi.app.ui.sign.signup.verify.presenter;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResAccountActivate;
import com.tosslab.jandi.app.ui.sign.signup.verify.exception.VerifyNetworkException;
import com.tosslab.jandi.app.ui.sign.signup.verify.model.SignUpVerifyModel;
import com.tosslab.jandi.app.ui.sign.signup.verify.view.SignUpVerifyView;
import com.tosslab.jandi.app.utils.SignOutUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrResendVerificationEmail;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrSignUp;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import javax.inject.Inject;

import rx.Completable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class SignUpVerifyPresenter {
    public static final int EXPIRED_VERIFICATION_CODE = 40005;
    public static final int INVALIDATE_VERIFICATION_CODE = 40006;

    SignUpVerifyModel model;
    SignUpVerifyView view;

    @Inject
    public SignUpVerifyPresenter(SignUpVerifyModel model, SignUpVerifyView view) {
        this.model = model;
        this.view = view;
    }

    public void verifyCode(String email, String verificationCode) {
        view.showProgress();

        Completable.fromCallable(() -> {
            ResAccountActivate accountActivate = model.requestSignUpVerify(email, verificationCode);
            LogUtil.e(accountActivate.toString());
            SignOutUtil.removeSignData();
            model.setAccountInfo(accountActivate);
            SprinklrSignUp.sendLog();
            AnalyticsUtil.flushSprinkler();

            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.hideProgress();
                    view.showToast(JandiApplication.getContext().getResources().getString(R.string.jandi_welcome_message));
                    view.moveToSelectTeam();
                }, t -> {
                    if (t instanceof VerifyNetworkException) {
                        VerifyNetworkException e = (VerifyNetworkException) t;
                        view.hideProgress();

                        LogUtil.d(e.getErrorInfo());
                        int errCode = e.errCode;
                        SprinklrSignUp.trackFail(e.errCode);
                        AnalyticsUtil.flushSprinkler();

                        switch (errCode) {
                            case EXPIRED_VERIFICATION_CODE:
                                view.showExpiredVerificationCode();
                                break;
                            case INVALIDATE_VERIFICATION_CODE:
                                int tryCount = e.getTryCount();
                                if (tryCount == VerifyNetworkException.NONE_TRY_COUNT) {
                                    view.showErrorToast(JandiApplication.getContext().getResources().getString(R.string.err_network));
                                    return;
                                }
                                view.showInvalidVerificationCode(tryCount);
                                break;
                            default:
                                e.printStackTrace();
                                view.showErrorToast(JandiApplication.getContext().getResources().getString(R.string.err_network));
                                break;
                        }

                    }
                });
    }

    public void requestNewVerificationCode(String email) {
        view.showProgress();

        Completable.fromCallable(() -> {
            model.requestNewVerificationCode(email);
            SprinklrResendVerificationEmail.sendLog(email);
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.hideProgress();

                    String successEmailText = JandiApplication.getContext().getResources()
                            .getString(R.string.jandi_signup_send_new_verification_code, email);
                    view.showToast(successEmailText);
                    view.changeExplainText();
                    view.clearVerifyCode();
                }, t -> {
                    t.printStackTrace();
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        SprinklrResendVerificationEmail.sendFailLog(e.getResponseCode());
                    }
                    view.hideProgress();
                    view.showErrorToast(JandiApplication.getContext().getResources().getString(R.string.err_network));
                });
    }
}
