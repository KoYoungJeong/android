package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.component;

import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.StarredListFragment;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.module.StarredListModule;

import dagger.Component;

/**
 * Created by tonyjs on 2016. 8. 9..
 */
@Component(modules = {StarredListModule.class})
public interface StarredListComponent {

    void inject(StarredListFragment fragment);

}
