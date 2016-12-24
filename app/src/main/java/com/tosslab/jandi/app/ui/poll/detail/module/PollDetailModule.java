package com.tosslab.jandi.app.ui.poll.detail.module;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.poll.detail.adapter.PollDetailAdapter;
import com.tosslab.jandi.app.ui.poll.detail.adapter.model.PollDetailDataModel;
import com.tosslab.jandi.app.ui.poll.detail.adapter.view.PollDetailDataView;
import com.tosslab.jandi.app.ui.poll.detail.presenter.PollDetailPresenter;
import com.tosslab.jandi.app.ui.poll.detail.presenter.PollDetailPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 16. 6. 14..
 */
@Module(includes = ApiClientModule.class)
public class PollDetailModule {

    private final PollDetailPresenter.View pollDetailView;
    private final PollDetailAdapter pollDetailAdapter;
    public PollDetailModule(PollDetailPresenter.View pollDetailView, PollDetailAdapter pollDetailAdapter) {
        this.pollDetailView = pollDetailView;
        this.pollDetailAdapter = pollDetailAdapter;
    }

    @Provides
    public PollDetailPresenter providesPollDetailPresenter(PollDetailPresenterImpl pollDetailPresenter) {
        return pollDetailPresenter;
    }

    @Provides
    public PollDetailPresenter.View providesPollDetailView() {
        return pollDetailView;
    }

    @Provides
    public PollDetailDataModel providesPollDetailDataModel() {
        return pollDetailAdapter;
    }

    @Provides
    public PollDetailDataView providesPollDetailDataView() {
        return pollDetailAdapter;
    }

}
