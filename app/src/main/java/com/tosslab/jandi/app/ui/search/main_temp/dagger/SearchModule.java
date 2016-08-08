package com.tosslab.jandi.app.ui.search.main_temp.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.SearchAdapter;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.SearchAdapterDataModel;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.SearchAdapterViewModel;
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
    private SearchAdapter searchAdapter;

    public SearchModule(SearchPresenter.View view, SearchAdapter adapter) {
        this.view = view;
        this.searchAdapter = adapter;
    }

    @Provides
    public SearchAdapterDataModel providesSearchAdapterDataModel() {
        return searchAdapter;
    }

    @Provides
    public SearchAdapterViewModel providesSearchAdapterViewModel() {
        return searchAdapter;
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