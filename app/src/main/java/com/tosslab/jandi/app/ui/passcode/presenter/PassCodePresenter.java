package com.tosslab.jandi.app.ui.passcode.presenter;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.passcode.PassCodeActivity;
import com.tosslab.jandi.app.ui.passcode.model.PassCodeModel;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

/**
 * Created by tonyjs on 15. 10. 13..
 */
@EBean
public class PassCodePresenter {

    @Bean
    PassCodeModel model;

    View view;

    public void setView(View view) {
        this.view = view;
    }

    public void onPassCodeInput(int passCode, int mode) {
        boolean putPassCode = model.putPassCode(passCode);
        if (!putPassCode) {
            return;
        }

        int passCodeLength = model.getPassCodeLength();
        view.checkPassCode(passCodeLength);

        if (passCodeLength < PassCodeModel.MAX_PASSCODE_LENGTH) {
            return;
        }

        if (mode == PassCodeActivity.MODE_TO_SAVE_PASSCODE
                || mode == PassCodeActivity.MODE_TO_MODIFY_PASSCODE) {
            String previousPassCode = model.getPreviousPassCode();
            if (!TextUtils.isEmpty(previousPassCode)) {
                boolean validate = model.validatePassCode(previousPassCode);
                if (validate) {
                    model.savePassCode();
                    view.showSuccess();
                } else {
                    view.showFail();
                }
            } else {
                model.setPreviousPassCode(model.getPassCode());
                model.clearPassCode();
                view.clearPassCodeCheckerWithDelay();
                view.showNeedToValidateText();
            }
            return;
        }

        String savedPassCode = JandiPreference.getPassCode(JandiApplication.getContext());
        boolean validate = model.validatePassCode(savedPassCode);
        if (validate) {
            view.showSuccess();
        } else {
            view.showFail();
        }
    }

    public void onDeletePassCode() {
        model.popPassCode();
        view.checkPassCode(model.getPassCodeLength());
    }

    public void clearPassCode() {
        model.setPreviousPassCode(null);
        model.clearPassCode();
        view.clearPassCodeChecker();
    }

    public interface View {
        void checkPassCode(int passCodeLength);

        void clearPassCodeChecker();

        void clearPassCodeCheckerWithDelay();

        void showNeedToValidateText();

        void showFail();

        void showSuccess();
    }

}
