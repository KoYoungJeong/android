package com.tosslab.jandi.app.ui.carousel.component;

import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity;
import com.tosslab.jandi.app.ui.carousel.module.CarouselViewerModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by tonyjs on 2016. 8. 3..
 */
@Component(modules = {CarouselViewerModule.class})
public interface CarouselViewerComponent {
    void inject(CarouselViewerActivity activity);
}
