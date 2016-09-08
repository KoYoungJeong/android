package com.tosslab.jandi.app.ui.maintab.tabs.file.dagger;

import com.tosslab.jandi.app.ui.maintab.tabs.file.FileListFragment;

import dagger.Component;

//import com.tosslab.jandi.app.ui.maintab.tabs.file.FileListFragment;

/**
 * Created by tee on 16. 6. 28..
 */

@Component(modules = FileListModule.class)
public interface FileListComponent {
    void inject(FileListFragment fragment);
}
