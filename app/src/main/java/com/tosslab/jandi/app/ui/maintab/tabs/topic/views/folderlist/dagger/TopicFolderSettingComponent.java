package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.dagger;


import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.TopicFolderSettingActivity;

import dagger.Component;

@Component(modules = {ApiClientModule.class, TopicFolderSettingModule.class})
public interface TopicFolderSettingComponent {
    void inject(TopicFolderSettingActivity activity);
}
