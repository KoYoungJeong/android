package com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.dagger;

import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.adapter.PollListAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.adapter.model.PollListDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.adapter.view.PollListDataView;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.presenter.PollListPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.presenter.PollListPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class PollListModule {

    private final PollListPresenter.View pollListView;
    private final PollListAdapter pollListAdapter;

    public PollListModule(PollListPresenter.View pollListView, PollListAdapter pollListAdapter) {
        this.pollListView = pollListView;
        this.pollListAdapter = pollListAdapter;
    }

    @Provides
    PollListPresenter providesPollListPresenter(PollListPresenterImpl pollListPresenter) {
        return pollListPresenter;
    }

    @Provides
    PollListDataModel providesPollListDataModel() {
        return pollListAdapter;
    }

    @Provides
    PollListDataView providesPollListDataView() {
        return pollListAdapter;
    }

    @Provides
    PollListPresenter.View providesPollListView() {
        return pollListView;
    }

}
