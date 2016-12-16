package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.dagger;


import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.presenter.TopicFolderSettingPresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class TopicFolderSettingModule {
    private final TopicFolderSettingPresenter.View view;

    public TopicFolderSettingModule(TopicFolderSettingPresenter.View view) {
        this.view = view;
    }

    @Provides
    TopicFolderSettingPresenter.View view() {
        return view;
    }

}
