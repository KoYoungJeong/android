package com.tosslab.jandi.app.ui.file.upload.preview.dagger;


import com.tosslab.jandi.app.ui.file.upload.preview.presenter.FileUploadPresenter;
import com.tosslab.jandi.app.ui.file.upload.preview.presenter.FileUploadPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class FileUploadModule {
    private final FileUploadPresenter.View view;

    public FileUploadModule(FileUploadPresenter.View view) {
        this.view = view;
    }

    @Provides
    FileUploadPresenter.View view() {
        return view;
    }

    @Provides
    FileUploadPresenter presenter(FileUploadPresenterImpl presenter) {
        return presenter;
    }
}
