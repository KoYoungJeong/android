package com.tosslab.jandi.app.ui.maintab.tabs.file.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.maintab.tabs.file.FileListFragment;

import dagger.Component;

@Component(modules = {ApiClientModule.class, FileListModule.class})
public interface FileListComponent {
    void inject(FileListFragment fragment);
}
