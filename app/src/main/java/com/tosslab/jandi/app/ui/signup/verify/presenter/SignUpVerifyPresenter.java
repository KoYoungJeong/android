package com.tosslab.jandi.app.ui.signup.verify.presenter;

import android.content.Context;

import com.tosslab.jandi.app.R;
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
        view.showProgress();

        String verificationCode = view.getVerificationCode();

        try {
            ResAccountInfo accountInfo = model.requestSignUpVerify(email, verificationCode);

            view.hideProgress();
            view.setResult(accountInfo);
        } catch (VerifyNetworkException e) {
            view.hideProgress();

            LogUtil.d(e.getErrorInfo() + " , Response Body : " + e.httpBody);
            int errCode = e.errCode;
            if (errCode == EXPIRED_VERIFICATION_CODE) {
                view.showErrorToast("인증 코드가 만료되었습니다. 이메일을 다시 보내주세요.");
            } else if (errCode == INVALIDATE_VERIFICATION_CODE) {
                int tryCount = e.getTryCount();
                if (tryCount == VerifyNetworkException.NONE_TRY_COUNT) {
                    view.showErrorToast(context.getResources().getString(R.string.err_network));
                    return;
                }
                view.showInvalidVerificationCode(tryCount);
            } else {
                view.showErrorToast(context.getResources().getString(R.string.err_network));
            }
        }
    }

    @Background
    public void requestNewVerificationCode(String email) {
        view.showProgress();

        try {
            model.requestNewVerificationCode(email);
            view.hideProgress();
        } catch (JandiNetworkException e) {
            view.hideProgress();
            LogUtil.d(e.getErrorInfo() + " , Response Body : " + e.httpBody);
            view.showErrorToast(context.getResources().getString(R.string.err_network));
        }
    }
}
