package com.tosslab.jandi.app.ui.passcode.fingerprint.dagger;


import com.tosslab.jandi.app.ui.passcode.fingerprint.FingerprintAuthDialogFragment;

import dagger.Component;

@Component(modules = FingerprintAuthModule.class)
public interface FingerprintAuthComponent {
    void inject(FingerprintAuthDialogFragment fragment);
}
