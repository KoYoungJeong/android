package com.tosslab.jandi.app.ui.poll.create.module;

import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.poll.create.model.PollCreateModel;
import com.tosslab.jandi.app.ui.poll.create.presenter.PollCreatePresenter;
import com.tosslab.jandi.app.ui.poll.create.presenter.PollCreatePresenterImpl;

import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 16. 6. 20..
 */
@Module(includes = ApiClientModule.class)
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

    @Provides
    @Singleton
    public PollCreateModel providesPollCreateModel(Lazy<PollApi> pollApi) {
        return new PollCreateModel(pollApi);
    }


}
