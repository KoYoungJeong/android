package com.tosslab.jandi.app.ui.search.file.dagger;

import com.tosslab.jandi.app.ui.search.file.view.FileSearchActivity;

import dagger.Component;

@Component(modules = FileSearchModule.class)
public interface FileSearchComponent {
    void inject(FileSearchActivity activity);
}
