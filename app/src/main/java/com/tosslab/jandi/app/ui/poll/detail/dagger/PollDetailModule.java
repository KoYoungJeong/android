package com.tosslab.jandi.app.ui.poll.detail.dagger;

import com.tosslab.jandi.app.ui.poll.detail.adapter.PollDetailAdapter;
import com.tosslab.jandi.app.ui.poll.detail.adapter.model.PollDetailDataModel;
import com.tosslab.jandi.app.ui.poll.detail.adapter.view.PollDetailDataView;
import com.tosslab.jandi.app.ui.poll.detail.presenter.PollDetailPresenter;
import com.tosslab.jandi.app.ui.poll.detail.presenter.PollDetailPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class PollDetailModule {

    private final PollDetailPresenter.View pollDetailView;
    private final PollDetailAdapter pollDetailAdapter;

    public PollDetailModule(PollDetailPresenter.View pollDetailView, PollDetailAdapter pollDetailAdapter) {
        this.pollDetailView = pollDetailView;
        this.pollDetailAdapter = pollDetailAdapter;
    }

    @Provides
    PollDetailPresenter providesPollDetailPresenter(PollDetailPresenterImpl pollDetailPresenter) {
        return pollDetailPresenter;
    }

    @Provides
    PollDetailPresenter.View providesPollDetailView() {
        return pollDetailView;
    }

    @Provides
    PollDetailDataModel providesPollDetailDataModel() {
        return pollDetailAdapter;
    }

    @Provides
    PollDetailDataView providesPollDetailDataView() {
        return pollDetailAdapter;
    }

}
