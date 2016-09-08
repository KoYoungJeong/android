package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.module;

import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.StarredListAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.model.StarredListDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.view.StarredListDataView;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.model.StarredListModel;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.presentor.StarredListPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.presentor.StarredListPresenterImpl;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 2016. 8. 9..
 */
@Module(includes = ApiClientModule.class)
public class StarredListModule {

    private StarredListAdapter starredListAdapter;
    private StarredListPresenter.View starredListView;

    public StarredListModule(StarredListAdapter starredListAdapter,
                             StarredListPresenter.View starredListView) {
        this.starredListAdapter = starredListAdapter;
        this.starredListView = starredListView;
    }

    @Provides
    public StarredListModel providesStarredListModel(Lazy<MessageApi> messageApi) {
        return new StarredListModel(messageApi);
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
