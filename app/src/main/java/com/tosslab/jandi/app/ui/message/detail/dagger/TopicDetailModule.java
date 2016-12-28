package com.tosslab.jandi.app.ui.message.detail.dagger;

import com.tosslab.jandi.app.ui.message.detail.presenter.TopicDetailPresenter;
import com.tosslab.jandi.app.ui.message.detail.presenter.TopicDetailPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class TopicDetailModule {

    private TopicDetailPresenter.View view;

    public TopicDetailModule(TopicDetailPresenter.View view) {
        this.view = view;
    }

    @Provides
    TopicDetailPresenter.View provideView() {
        return view;
    }

    @Provides
    TopicDetailPresenter provideTopicDetailPresenter(TopicDetailPresenterImpl presenter) {
        return presenter;
    }
}
