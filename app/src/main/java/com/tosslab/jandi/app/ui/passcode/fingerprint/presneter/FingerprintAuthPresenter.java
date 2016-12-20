package com.tosslab.jandi.app.ui.passcode.fingerprint.presneter;

import android.annotation.TargetApi;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.util.Log;

import com.tosslab.jandi.app.ui.passcode.fingerprint.model.FingerprintAuthModel;
import com.tosslab.jandi.app.ui.passcode.fingerprint.view.FingerprintAuthView;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintAuthPresenter extends FingerprintManager.AuthenticationCallback {
    private static final String TAG = FingerprintAuthPresenter.class.getSimpleName();

    FingerprintAuthModel model;

    private FingerprintAuthView view;
    private boolean isCancelled;

    @Inject
    public FingerprintAuthPresenter(FingerprintAuthView view, FingerprintAuthModel model) {
        this.view = view;
        this.model = model;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public void onStartListeningFingerprint() {
        model.getKeyStoreObservable()
                .concatMap(model::getSecretKeyObservable)
                .concatMap(model::initCipherObservable)
                .subscribeOn(Schedulers.trampoline())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cipher -> {
                    view.startListening(new FingerprintManager.CryptoObject(cipher));
                }, e -> LogUtil.e(TAG, Log.getStackTraceString(e)));
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        if (!isCancelled) {
            view.showAuthenticationError(errorCode, errString);
        }
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        view.showFingerprintAuthFailed();
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
        view.showAuthenticationHelpDialog(helpString);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        view.setFingerprintAuthSuccess();
    }

}
