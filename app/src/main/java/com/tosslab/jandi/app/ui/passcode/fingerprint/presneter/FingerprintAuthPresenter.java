package com.tosslab.jandi.app.ui.passcode.fingerprint.presneter;

import android.annotation.TargetApi;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.util.Log;

import com.tosslab.jandi.app.ui.passcode.fingerprint.model.FingerprintAuthModel;
import com.tosslab.jandi.app.ui.passcode.fingerprint.view.FingerprintAuthView;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tonyjs on 16. 3. 25..
 */
@EBean
@TargetApi(Build.VERSION_CODES.M)
public class FingerprintAuthPresenter extends FingerprintManager.AuthenticationCallback {

    @Bean
    FingerprintAuthModel model;

    private FingerprintAuthView view;
    private boolean isCancelled;

    public void setView(FingerprintAuthView view) {
        this.view = view;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public void onStartListeningFingerprint() {
        model.getKeyStoreObservable()
                .concatMap(model::getSecretKeyObservable)
                .concatMap(model::initCipherObservable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cipher -> {
                    view.startListening(new FingerprintManager.CryptoObject(cipher));
                }, e -> LogUtil.e("tony", Log.getStackTraceString(e)));
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
