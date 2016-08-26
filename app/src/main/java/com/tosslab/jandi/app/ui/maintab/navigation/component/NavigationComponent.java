package com.tosslab.jandi.app.ui.maintab.navigation.component;

import com.tosslab.jandi.app.ui.maintab.navigation.NavigationFragment;
import com.tosslab.jandi.app.ui.maintab.navigation.module.NavigationModule;

import dagger.Component;

/**
 * Created by tonyjs on 2016. 8. 17..
 */
@Component(modules = {NavigationModule.class})
public interface NavigationComponent {

    void inject(NavigationFragment fragment);

}
