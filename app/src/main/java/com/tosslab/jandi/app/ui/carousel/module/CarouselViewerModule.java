package com.tosslab.jandi.app.ui.carousel.module;

import android.content.ClipboardManager;
import android.content.Context;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.client.file.FileApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.carousel.model.CarouselViewerModel;
import com.tosslab.jandi.app.ui.carousel.presenter.CarouselViewerPresenter;
import com.tosslab.jandi.app.ui.carousel.presenter.CarouselViewerPresenterImpl;
import com.tosslab.jandi.app.ui.filedetail.model.FileDetailModel;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 2016. 8. 3..
 */
@Module(includes = ApiClientModule.class)
public class CarouselViewerModule {

    private final CarouselViewerPresenter.View carouselView;

    public CarouselViewerModule(CarouselViewerPresenter.View carouselView) {
        this.carouselView = carouselView;
    }

    @Provides
    public ClipboardManager providesClipboardManager() {
        return (ClipboardManager)
                JandiApplication.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Provides
    public CarouselViewerModel providesCarouselViewerModel(Lazy<FileApi> fileApi) {
        return new CarouselViewerModel(fileApi);
    }

    @Provides
    public CarouselViewerPresenter providesCarouselViewerPresenter(
            CarouselViewerModel carouselViewerModel, FileDetailModel fileDetailModel) {
        return new CarouselViewerPresenterImpl(carouselViewerModel, fileDetailModel, carouselView);
    }

    @Provides
    public CarouselViewerPresenter.View providesCarouselViewerView() {
        return carouselView;
    }

}
