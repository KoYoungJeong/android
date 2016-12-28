package com.tosslab.jandi.app.ui.message.detail.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.message.detail.view.TopicDetailFragment;

import dagger.Component;

@Component(modules = {ApiClientModule.class, TopicDetailModule.class})
public interface TopicDetailComponent {
    void inject(TopicDetailFragment fragment);

}
