package com.tosslab.jandi.app.ui.poll.create.dagger;

import com.tosslab.jandi.app.ui.poll.create.presenter.PollCreatePresenter;
import com.tosslab.jandi.app.ui.poll.create.presenter.PollCreatePresenterImpl;

import dagger.Module;
import dagger.Provides;


@Module
public class PollCreateModule {

    private final PollCreatePresenter.View pollCreateView;

    public PollCreateModule(PollCreatePresenter.View view) {
        pollCreateView = view;
    }

    @Provides
    public PollCreatePresenter providesPollCreatePresenter(PollCreatePresenterImpl pollCreatePresenter) {
        return pollCreatePresenter;
    }

    @Provides
    public PollCreatePresenter.View providesPollCreateView() {
        return pollCreateView;
    }

}
