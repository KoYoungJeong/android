package com.tosslab.jandi.app.ui.filedetail.dagger;


import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerViewModel_;
import com.tosslab.jandi.app.ui.filedetail.FileDetailPresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class FileDetailModule {
    private final FileDetailPresenter.View view;

    public FileDetailModule(FileDetailPresenter.View view) {
        this.view = view;
    }

    @Provides
    FileDetailPresenter.View view() {
        return view;
    }

    @Provides
    StickerViewModel stickerViewModel() {
        return StickerViewModel_.getInstance_(JandiApplication.getContext());
    }
}
