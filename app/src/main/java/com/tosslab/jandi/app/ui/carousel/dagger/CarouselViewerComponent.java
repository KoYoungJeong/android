package com.tosslab.jandi.app.ui.carousel.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity;

import dagger.Component;

/**
 * Created by tonyjs on 2016. 8. 3..
 */
@Component(modules = {ApiClientModule.class, CarouselViewerModule.class})
public interface CarouselViewerComponent {
    void inject(CarouselViewerActivity activity);
}
