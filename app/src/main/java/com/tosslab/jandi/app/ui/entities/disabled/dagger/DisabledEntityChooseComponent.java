package com.tosslab.jandi.app.ui.entities.disabled.dagger;


import com.tosslab.jandi.app.ui.entities.disabled.view.DisabledEntityChooseActivity;

import dagger.Component;

@Component(modules = DisabledEntityChooseModule.class)
public interface DisabledEntityChooseComponent {
    void inject(DisabledEntityChooseActivity activity);
}
