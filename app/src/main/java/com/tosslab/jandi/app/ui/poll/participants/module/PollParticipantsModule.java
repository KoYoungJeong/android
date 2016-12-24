package com.tosslab.jandi.app.ui.poll.participants.module;

import com.tosslab.jandi.app.ui.poll.participants.presenter.PollParticipantsPresenter;
import com.tosslab.jandi.app.ui.poll.participants.presenter.PollParticipantsPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class PollParticipantsModule {
    private final PollParticipantsPresenter.View pollParticipantsView;

    public PollParticipantsModule(PollParticipantsPresenter.View pollParticipantsView) {
        this.pollParticipantsView = pollParticipantsView;
    }

    @Provides
    public PollParticipantsPresenter providesPollParticipantsPresenter(
            PollParticipantsPresenterImpl pollParticipantsPresenter) {
        return pollParticipantsPresenter;
    }

    @Provides
    public PollParticipantsPresenter.View providesPollDetailView() {
        return pollParticipantsView;
    }

}
