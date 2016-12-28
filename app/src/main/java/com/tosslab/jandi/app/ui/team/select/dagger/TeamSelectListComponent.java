package com.tosslab.jandi.app.ui.team.select.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.team.select.TeamSelectListActivity;

import dagger.Component;

@Component(modules = {ApiClientModule.class, TeamSelectListModule.class})
public interface TeamSelectListComponent {
    void inject(TeamSelectListActivity activity);
}