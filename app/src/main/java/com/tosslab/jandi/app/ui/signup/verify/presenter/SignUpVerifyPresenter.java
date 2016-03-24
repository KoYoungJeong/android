package com.tosslab.jandi.app.ui.signup.verify.presenter;

import android.content.Context;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResAccountActivate;
import com.tosslab.jandi.app.ui.signup.verify.exception.VerifyNetworkException;
import com.tosslab.jandi.app.ui.signup.verify.model.SignUpVerifyModel;
import com.tosslab.jandi.app.ui.signup.verify.view.SignUpVerifyView;
import com.tosslab.jandi.app.utils.SignOutUtil;
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

    @Background
    public void verifyCode(String email, String verificationCode) {
        view.showProgress();

        try {
            ResAccountActivate accountActivate = model.requestSignUpVerify(email, verificationCode);
            LogUtil.e(accountActivate.toString());

            view.hideProgress();
            view.showToast(context.getResources().getString(R.string.jandi_welcome_message));

            SignOutUtil.removeSignData();
            model.setAccountInfo(accountActivate);
            model.trackSignUpSuccessAndFlush(accountActivate.getAccount());

            view.moveToAccountHome();
        } catch (VerifyNetworkException e) {
            view.hideProgress();

            LogUtil.d(e.getErrorInfo());
            int errCode = e.errCode;
            model.trackSignUpFailAndFlush(errCode);
            switch (errCode) {
                case EXPIRED_VERIFICATION_CODE:

                    view.showExpiredVerificationCode();
                    break;
                case INVALIDATE_VERIFICATION_CODE:
                    int tryCount = e.getTryCount();
                    if (tryCount == VerifyNetworkException.NONE_TRY_COUNT) {
                        view.showErrorToast(context.getResources().getString(R.string.err_network));
                        return;
                    }
                    view.showInvalidVerificationCode(tryCount);
                    break;
                default:
                    e.printStackTrace();
                    view.showErrorToast(context.getResources().getString(R.string.err_network));
                    break;
            }
        }
    }

    @Background
    public void requestNewVerificationCode(String email) {
        view.showProgress();

        try {
            model.requestNewVerificationCode(email);
            view.hideProgress();

            String successEmailText = context.getResources()
                    .getString(R.string.jandi_signup_send_new_verification_code, email);
            view.showToast(successEmailText);
            view.changeExplainText();
            view.clearVerifyCode();
        } catch (RetrofitError e) {
            e.printStackTrace();
            view.hideProgress();
            view.showErrorToast(context.getResources().getString(R.string.err_network));
        }
    }
}
