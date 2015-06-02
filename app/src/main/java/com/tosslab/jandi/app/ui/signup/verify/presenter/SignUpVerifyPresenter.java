package com.tosslab.jandi.app.ui.signup.verify.presenter;

import android.content.Context;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResAccountActivate;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.signup.verify.model.SignUpVerifyModel;
import com.tosslab.jandi.app.ui.signup.verify.to.VerifyNetworkException;
import com.tosslab.jandi.app.ui.signup.verify.view.SignUpVerifyView;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * Created by tonyjs on 15. 5. 19..
 */
@EBean
public class SignUpVerifyPresenter {
    public static final int EXPIRED_VERIFICATION_CODE = 40005;
    public static final int INVALIDATE_VERIFICATION_CODE = 40006;

    @RootContext
    Context context;

    @Bean
    SignUpVerifyModel model;

    SignUpVerifyView view;

    public void setView(SignUpVerifyView signUpVerifyView) {
        view = signUpVerifyView;
    }

    public void validateVerificationCode() {
        String verifyCode = view.getVerificationCode();
        boolean verified = model.isValidVerificationCode(verifyCode);
        view.setVerifyButtonEnabled(verified);
    }

    @Background
    public void verifyCode(String email) {
        view.setValidateTextColor();
        view.hideInvalidVerificationCode();

        view.showProgress();

        String verificationCode = view.getVerificationCode();

        try {
            ResAccountActivate accountActivate = model.requestSignUpVerify(email, verificationCode);
            LogUtil.e(accountActivate.toString());
            view.hideProgress();
            view.showToast(context.getResources().getString(R.string.jandi_welcome_message));
            model.setResult(accountActivate);
            view.moveToAccountHome();
        } catch (VerifyNetworkException e) {
            view.hideProgress();

            LogUtil.d(e.getErrorInfo() + " , Response Body : " + e.httpBody);
            int errCode = e.errCode;
            if (errCode == EXPIRED_VERIFICATION_CODE) {
                view.hideResend();

                view.showExpiredVerificationCode();
            } else if (errCode == INVALIDATE_VERIFICATION_CODE) {
                int tryCount = e.getTryCount();
                if (tryCount == VerifyNetworkException.NONE_TRY_COUNT) {
                    view.showErrorToast(context.getResources().getString(R.string.err_network));
                    return;
                }
                view.setInvalidateTextColor();
                view.showInvalidVerificationCode(tryCount);
            } else {
                view.showErrorToast(context.getResources().getString(R.string.err_network));
            }
        }
    }

    @Background
    public void requestNewVerificationCode(String email) {
        view.hideInvalidVerificationCode();
        view.clearVerificationCode();

        view.showProgress();

        try {
            model.requestNewVerificationCode(email);
            view.hideProgress();
            view.showResend();
            String successEmailText = context.getResources()
                    .getString(R.string.jandi_signup_send_new_verification_code);
            successEmailText = String.format(successEmailText, email);
            view.showToast(successEmailText);
        } catch (JandiNetworkException e) {
            view.hideProgress();
            LogUtil.d(e.getErrorInfo() + " , Response Body : " + e.httpBody);
            view.showErrorToast(context.getResources().getString(R.string.err_network));
        }
    }
}
