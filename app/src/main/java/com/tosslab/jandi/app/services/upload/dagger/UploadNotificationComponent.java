package com.tosslab.jandi.app.services.upload.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.services.upload.UploadNotificationActivity;

import dagger.Component;

@Component(modules = ApiClientModule.class)
public interface UploadNotificationComponent {
    void inject(UploadNotificationActivity activity);
}
