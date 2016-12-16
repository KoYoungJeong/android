package com.tosslab.jandi.app.ui.maintab.tabs.topic.dagger;


import com.tosslab.jandi.app.ui.maintab.tabs.topic.presenter.MainTopicListPresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class MainTopicListModule {

    private final MainTopicListPresenter.View view;

    public MainTopicListModule(MainTopicListPresenter.View view) {
        this.view = view;
    }

    @Provides
    MainTopicListPresenter.View view() {
        return view;
    }
}
