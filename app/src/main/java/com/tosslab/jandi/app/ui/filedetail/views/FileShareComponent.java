package com.tosslab.jandi.app.ui.filedetail.views;


import com.tosslab.jandi.app.network.dagger.ApiClientModule;

import dagger.Component;

@Component(modules = ApiClientModule.class)
public interface FileShareComponent {
    void inject(FileSharedEntityChooseActivity activity);
}
