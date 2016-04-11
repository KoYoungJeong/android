package com.tosslab.jandi.app.ui.passcode.fingerprint.view;

import android.hardware.fingerprint.FingerprintManager;

/**
 * Created by tonyjs on 16. 3. 25..
 */
public interface FingerprintAuthView {

    void startListening(FingerprintManager.CryptoObject cryptoObject);

    void showFingerprintAuthFailed();

    void setFingerprintAuthSuccess();

    void showAuthenticationHelpDialog(CharSequence helpString);

    void showAuthenticationError(int errorCode, CharSequence errString);
}
