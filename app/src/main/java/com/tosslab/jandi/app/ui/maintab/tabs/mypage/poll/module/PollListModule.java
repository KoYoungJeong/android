package com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.module;

import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.adapter.PollListAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.adapter.model.PollListDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.adapter.view.PollListDataView;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.model.PollListModel;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.presenter.PollListPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.presenter.PollListPresenterImpl;

import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 16. 6. 28..
 */
@Module(includes = ApiClientModule.class)
public class PollListModule {

    private final PollListPresenter.View pollListView;
    private final PollListAdapter pollListAdapter;

    public PollListModule(PollListPresenter.View pollListView, PollListAdapter pollListAdapter) {
        this.pollListView = pollListView;
        this.pollListAdapter = pollListAdapter;
    }

    @Provides
    public PollListPresenter providesPollListPresenter(PollListPresenterImpl pollListPresenter) {
        return pollListPresenter;
    }

    @Provides
    public PollListDataModel providesPollListDataModel() {
        return pollListAdapter;
    }

    @Provides
    public PollListDataView providesPollListDataView() {
        return pollListAdapter;
    }

    @Provides
    public PollListPresenter.View providesPollListView() {
        return pollListView;
    }

    @Provides
    @Singleton
    public PollListModel providesPollListModel(Lazy<PollApi> pollApi) {
        return new PollListModel(pollApi);
    }


}
