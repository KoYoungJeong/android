package com.tosslab.jandi.app.ui.message.detail.dagger;

import com.tosslab.jandi.app.ui.message.detail.view.ChatDetailFragment;
import com.tosslab.jandi.app.ui.message.detail.view.TopicDetailFragment;

import dagger.Component;

@Component(modules = TopicDetailModule.class)
public interface TopicDetailComponent {
    void inject(TopicDetailFragment fragment);

    void inject(ChatDetailFragment fragment);
}
