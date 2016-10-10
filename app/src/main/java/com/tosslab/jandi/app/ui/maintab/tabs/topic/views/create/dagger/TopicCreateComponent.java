package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.dagger;


import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.TopicCreateActivity;

import dagger.Component;

@Component(modules = {TopicCreateModule.class})
public interface TopicCreateComponent {
    void inject(TopicCreateActivity activity);
}
