package com.tosslab.jandi.app.ui.poll.participants.module;

import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.poll.participants.model.PollParticipantsModel;
import com.tosslab.jandi.app.ui.poll.participants.presenter.PollParticipantsPresenter;
import com.tosslab.jandi.app.ui.poll.participants.presenter.PollParticipantsPresenterImpl;

import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 16. 6. 27..
 */
@Module(includes = ApiClientModule.class)
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

    @Provides
    @Singleton
    public PollParticipantsModel providesPollDetailModel(Lazy<PollApi> pollApi) {
        return new PollParticipantsModel(pollApi);
    }

}
