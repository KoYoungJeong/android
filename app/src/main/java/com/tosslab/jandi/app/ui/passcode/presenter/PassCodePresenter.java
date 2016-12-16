package com.tosslab.jandi.app.ui.passcode.presenter;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.passcode.model.PassCodeModel;
import com.tosslab.jandi.app.utils.JandiPreference;

import javax.inject.Inject;

public class PassCodePresenter {

    PassCodeModel model;
    View view;

    @Inject
    public PassCodePresenter(View view, PassCodeModel model) {
        this.model = model;
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

        // 패스코드를 설정하거나 바꾸는 경우
        if (model.isSetupMode(mode)) {
            setupPassCode();
        } else {
            // 일반 잠금해제 수행중인 경우
            validatePassCode();
        }
    }

    private void validatePassCode() {
        String savedPassCode = JandiPreference.getPassCode(JandiApplication.getContext());
        boolean validate = model.validatePassCode(savedPassCode);
        if (validate) {
            view.showSuccess();
        } else {
            view.showFail();
        }
    }

    private void setupPassCode() {
        String previousPassCode = model.getPreviousPassCode();
        // 선 입력된 패스코드가 없는 경우(첫번째 입력중인 경우)
        if (TextUtils.isEmpty(previousPassCode)) {
            model.setPreviousPassCode(model.getPassCode());
            model.clearPassCode();
            view.clearPassCodeCheckerWithDelay();
            view.showNeedToValidateText();
        } else {
            boolean validate = model.validatePassCode(previousPassCode);
            if (validate) {
                model.savePassCode();
                view.showSuccess();
            } else {
                view.showFail();
            }
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

    public void onDetermineUseFingerprint() {
        if (!model.isUserWantsFingerPrintToAuth()) {
            return;
        }

        if (model.canUseFingerprintToAuth()) {

            view.showUnLockFromFingerprintDialog();

        }
    }

    public interface View {
        void checkPassCode(int passCodeLength);

        void clearPassCodeChecker();

        void clearPassCodeCheckerWithDelay();

        void showNeedToValidateText();

        void showFail();

        void showSuccess();

        void showUnLockFromFingerprintDialog();

    }

}
