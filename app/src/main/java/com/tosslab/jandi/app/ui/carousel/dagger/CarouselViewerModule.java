package com.tosslab.jandi.app.ui.carousel.dagger;

import android.content.ClipboardManager;
import android.content.Context;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.carousel.presenter.CarouselViewerPresenter;
import com.tosslab.jandi.app.ui.carousel.presenter.CarouselViewerPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class CarouselViewerModule {

    private final CarouselViewerPresenter.View carouselView;

    public CarouselViewerModule(CarouselViewerPresenter.View carouselView) {
        this.carouselView = carouselView;
    }

    @Provides
    ClipboardManager providesClipboardManager() {
        return (ClipboardManager)
                JandiApplication.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Provides
    CarouselViewerPresenter.View providesCarouselViewerView() {
        return carouselView;
    }

    @Provides
    CarouselViewerPresenter presenter(CarouselViewerPresenterImpl presenter) {
        return presenter;
    }
}
