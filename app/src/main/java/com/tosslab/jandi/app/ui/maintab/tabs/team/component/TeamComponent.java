package com.tosslab.jandi.app.ui.maintab.tabs.team.component;

import com.tosslab.jandi.app.ui.maintab.tabs.team.TeamFragment;
import com.tosslab.jandi.app.ui.maintab.tabs.team.module.TeamModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by tonyjs on 16. 3. 15..
 */
@Component(modules = TeamModule.class)
@Singleton
public interface TeamComponent {

    void inject(TeamFragment fragment);

}
