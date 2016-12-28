package com.tosslab.jandi.app.ui.album.imagealbum.dagger;


import com.tosslab.jandi.app.ui.album.imagealbum.presenter.ImageAlbumPresenter;
import com.tosslab.jandi.app.ui.album.imagealbum.presenter.ImageAlbumPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class ImageAlbumFragModule {
    private final ImageAlbumPresenter.View view;

    public ImageAlbumFragModule(ImageAlbumPresenter.View view) {
        this.view = view;
    }

    @Provides
    ImageAlbumPresenter.View view() {
        return view;
    }

    @Provides
    ImageAlbumPresenter presenter(ImageAlbumPresenterImpl presenter) {
        return presenter;
    }
}
