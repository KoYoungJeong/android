package com.tosslab.jandi.app.ui.file.upload.preview.dagger;


import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity;

import dagger.Component;

@Component(modules = {ApiClientModule.class, FileUploadModule.class})
public interface FileUploadComponent {
    void inject(FileUploadPreviewActivity activity);
}
