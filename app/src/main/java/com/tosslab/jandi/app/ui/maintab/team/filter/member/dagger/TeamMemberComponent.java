package com.tosslab.jandi.app.ui.maintab.team.filter.member.dagger;


import com.tosslab.jandi.app.ui.maintab.team.filter.member.TeamMemberFragment;

import dagger.Component;

@Component(modules = TeamMemberModule.class)
public interface TeamMemberComponent {
    void inject(TeamMemberFragment fragment);
}
