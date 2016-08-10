package com.tosslab.jandi.app.ui.profile.modify.property.namestatus.dagger;


import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.profile.modify.property.namestatus.view.NameChangeFragment;
import com.tosslab.jandi.app.ui.profile.modify.property.namestatus.view.StatusChangeFragment;

import dagger.Component;

@Component(modules = {ApiClientModule.class, NameStatusModule.class})
public interface NameStatusComponent {
    void inject(NameChangeFragment fragment);

    void inject(StatusChangeFragment fragment);
}