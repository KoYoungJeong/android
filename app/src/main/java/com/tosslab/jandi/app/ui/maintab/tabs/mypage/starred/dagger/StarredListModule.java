package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.dagger;

import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.StarredListAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.model.StarredListDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.view.StarredListDataView;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.presentor.StarredListPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.presentor.StarredListPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class StarredListModule {

    private StarredListAdapter starredListAdapter;
    private StarredListPresenter.View starredListView;

    public StarredListModule(StarredListAdapter starredListAdapter,
                             StarredListPresenter.View starredListView) {
        this.starredListAdapter = starredListAdapter;
        this.starredListView = starredListView;
    }

    @Provides
    public StarredListPresenter providesStarredListPresenter(StarredListPresenterImpl starredListPresenter) {
        return starredListPresenter;
    }

    @Provides
    public StarredListPresenter.View providesStarredListView() {
        return starredListView;
    }

    @Provides
    public StarredListDataModel providesStarredListDataModel() {
        return starredListAdapter;
    }

    @Provides
    public StarredListDataView providesStarredListDataView() {
        return starredListAdapter;
    }

}
