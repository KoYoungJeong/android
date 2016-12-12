package com.tosslab.jandi.app.ui.search.main.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.search.main.SearchActivity;

import dagger.Component;

/**
 * Created by tee on 16. 7. 25..
 */
@Component(modules = {SearchModule.class, ApiClientModule.class})
public interface SearchComponent {
    void inject(SearchActivity activity);
}