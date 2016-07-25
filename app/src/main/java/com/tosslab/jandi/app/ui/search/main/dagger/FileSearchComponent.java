package com.tosslab.jandi.app.ui.search.main.dagger;

import com.tosslab.jandi.app.ui.search.main.view.FileSearchActivity;

import dagger.Component;

@Component(modules = FileSearchModule.class)
public interface FileSearchComponent {
    void inject(FileSearchActivity activity);
}
