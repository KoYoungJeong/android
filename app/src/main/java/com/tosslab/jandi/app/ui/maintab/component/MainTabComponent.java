package com.tosslab.jandi.app.ui.maintab.component;

import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.maintab.module.MainTabModule;

import dagger.Component;

/**
 * Created by tonyjs on 2016. 8. 23..
 */
@Component(modules = {MainTabModule.class})
public interface MainTabComponent {

    void inject(MainTabActivity activity);
}
