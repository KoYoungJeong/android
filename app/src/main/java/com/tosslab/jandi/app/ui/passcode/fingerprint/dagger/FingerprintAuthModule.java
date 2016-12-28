package com.tosslab.jandi.app.ui.passcode.fingerprint.dagger;


import com.tosslab.jandi.app.ui.passcode.fingerprint.view.FingerprintAuthView;

import dagger.Module;
import dagger.Provides;

@Module
public class FingerprintAuthModule {
    private final FingerprintAuthView view;

    public FingerprintAuthModule(FingerprintAuthView view) {
        this.view = view;
    }

    @Provides
    FingerprintAuthView view() {
        return view;
    }
}
