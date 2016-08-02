package com.tosslab.jandi.app.ui.search.main_temp.dagger;

import com.tosslab.jandi.app.ui.search.main_temp.SearchActivity;

import dagger.Component;

/**
 * Created by tee on 16. 7. 25..
 */
@Component(modules = {SearchModule.class})
public interface SearchComponent {
    void inject(SearchActivity activity);
}