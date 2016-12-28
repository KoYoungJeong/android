package com.tosslab.jandi.app.ui.profile.email.dagger;

import com.tosslab.jandi.app.ui.profile.email.EmailChooseActivity;

import dagger.Component;

/**
 * Created by tee on 2016. 12. 22..
 */

@Component(modules = EmailChooseModule.class)
public interface EmailChooseComponent {
    void inject(EmailChooseActivity activity);
}