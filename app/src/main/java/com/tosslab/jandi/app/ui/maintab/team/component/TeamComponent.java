package com.tosslab.jandi.app.ui.maintab.team.component;

import com.tosslab.jandi.app.ui.maintab.team.TeamFragment;
import com.tosslab.jandi.app.ui.maintab.team.module.TeamModule;

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
