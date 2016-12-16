package com.tosslab.jandi.app.ui.passcode.dagger;


import com.tosslab.jandi.app.ui.passcode.PassCodeActivity;

import dagger.Component;

@Component(modules = PassCodeModule.class)
public interface PassCodeComponent {
    void inject(PassCodeActivity activity);
}
