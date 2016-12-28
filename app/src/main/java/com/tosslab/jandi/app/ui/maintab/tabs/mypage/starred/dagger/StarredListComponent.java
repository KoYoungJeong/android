package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.StarredListFragment;

import dagger.Component;

@Component(modules = {ApiClientModule.class, StarredListModule.class})
public interface StarredListComponent {

    void inject(StarredListFragment fragment);

}
