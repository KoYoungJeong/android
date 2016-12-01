package com.tosslab.jandi.app.ui.share.file.dagger;


import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.share.file.FileShareFragment;

import dagger.Component;

@Component(modules = {FileShareModule.class, ApiClientModule.class})
public interface FileShareComponent {
    void inject(FileShareFragment fragment);
}
