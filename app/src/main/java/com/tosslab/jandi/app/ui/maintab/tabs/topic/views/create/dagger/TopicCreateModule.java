package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.dagger;


import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.presenter.TopicCreatePresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.presenter.TopicCreatePresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class TopicCreateModule {

    private TopicCreatePresenter.View view;

    public TopicCreateModule(TopicCreatePresenter.View view) {
        this.view = view;
    }

    @Provides
    TopicCreatePresenter presenter(TopicCreatePresenterImpl presenter) {
        return presenter;
    }

    @Provides
    TopicCreatePresenter.View view() {
        return view;
    }
}
