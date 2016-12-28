package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.dagger;


import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.TeamMemberFragment;

import dagger.Component;

@Component(modules = {ApiClientModule.class, TeamMemberModule.class})
public interface TeamMemberComponent {
    void inject(TeamMemberFragment fragment);
}
