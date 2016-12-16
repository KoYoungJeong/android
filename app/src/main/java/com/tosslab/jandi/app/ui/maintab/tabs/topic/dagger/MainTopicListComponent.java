package com.tosslab.jandi.app.ui.maintab.tabs.topic.dagger;


import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.MainTopicListFragment;

import dagger.Component;

@Component(modules = {ApiClientModule.class, MainTopicListModule.class})
public interface MainTopicListComponent {
    void inject(MainTopicListFragment fragment);
}
