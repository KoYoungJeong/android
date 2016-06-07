package com.tosslab.jandi.app.ui.maintab.teams.component;

import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.maintab.teams.module.TeamsModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by tonyjs on 16. 3. 21..
 */
@Component(modules = {TeamsModule.class})
@Singleton
public interface TeamsComponent {
    void inject(MainTabActivity activity);
}
