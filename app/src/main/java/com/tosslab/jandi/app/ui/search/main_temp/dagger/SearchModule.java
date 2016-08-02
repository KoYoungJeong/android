package com.tosslab.jandi.app.ui.search.main_temp.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.search.main_temp.presenter.SearchPresenter;
import com.tosslab.jandi.app.ui.search.main_temp.presenter.SearchPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tee on 16. 7. 25..
 */

@Module(includes = ApiClientModule.class)
public class SearchModule {
    private SearchPresenter.View view;

    public SearchModule(SearchPresenter.View view) {
        this.view = view;
    }

    @Provides
    public SearchPresenter.View provideSearchViewInPresenter() {
        return view;
    }

    @Provides
    public SearchPresenter provideSearchPresenter(SearchPresenterImpl searchPresenter) {
        return searchPresenter;
    }
}