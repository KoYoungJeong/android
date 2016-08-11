package com.tosslab.jandi.app.ui.starred.component;

import com.tosslab.jandi.app.ui.starred.StarredListFragment;
import com.tosslab.jandi.app.ui.starred.module.StarredListModule;

import dagger.Component;

/**
 * Created by tonyjs on 2016. 8. 9..
 */
@Component(modules = {StarredListModule.class})
public interface StarredListComponent {

    void inject(StarredListFragment fragment);

}
