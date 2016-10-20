package com.tosslab.jandi.app.ui.team.select.dagger;

import com.tosslab.jandi.app.ui.team.select.TeamSelectListActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by tee on 2016. 9. 27..
 */

@Component(modules = {TeamSelectListModule.class})
@Singleton
public interface TeamSelectListComponent {
    void inject(TeamSelectListActivity activity);
}