package com.tosslab.jandi.app.ui.maintab.file.dagger;

import com.tosslab.jandi.app.ui.maintab.file.FileListFragment;
import com.tosslab.jandi.app.ui.maintab.file.FileListFragmentV3;

import dagger.Component;

//import com.tosslab.jandi.app.ui.maintab.file.FileListFragmentV3;

/**
 * Created by tee on 16. 6. 28..
 */

@Component(modules = FileListModule.class)
public interface FileListComponent {
    void inject(FileListFragmentV3 fragment);
}
