package com.tosslab.jandi.app.ui.filedetail.dagger;


import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity;

import dagger.Component;

@Component(modules = {FileDetailModule.class, ApiClientModule.class})
public interface FileDetailComponent {
    void inject(FileDetailActivity activity);
}
