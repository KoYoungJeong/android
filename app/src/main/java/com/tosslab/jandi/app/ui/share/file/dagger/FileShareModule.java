package com.tosslab.jandi.app.ui.share.file.dagger;


import com.tosslab.jandi.app.ui.share.file.presenter.ImageSharePresenter;
import com.tosslab.jandi.app.ui.share.file.presenter.ImageSharePresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class FileShareModule {
    private final ImageSharePresenter.View view;

    public FileShareModule(ImageSharePresenter.View view) {
        this.view = view;
    }

    @Provides
    public ImageSharePresenter.View view() {
        return view;
    }

    @Provides
    public ImageSharePresenter presenter(ImageSharePresenterImpl presenter) {
        return presenter;
    }

}
