package com.tosslab.jandi.app.ui.album.videoalbum.dagger;


import com.tosslab.jandi.app.ui.album.videoalbum.presenter.VideoAlbumPresenter;
import com.tosslab.jandi.app.ui.album.videoalbum.presenter.VideoAlbumPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class VideoAlbumFragModule {
    private final VideoAlbumPresenter.View view;

    public VideoAlbumFragModule(VideoAlbumPresenter.View view) {
        this.view = view;
    }

    @Provides
    VideoAlbumPresenter.View view() {
        return view;
    }

    @Provides
    VideoAlbumPresenter presenter(VideoAlbumPresenterImpl presenter) {
        return presenter;
    }
}
