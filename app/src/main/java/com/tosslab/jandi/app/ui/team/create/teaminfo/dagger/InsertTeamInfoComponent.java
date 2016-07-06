package com.tosslab.jandi.app.ui.team.create.teaminfo.dagger;

import com.tosslab.jandi.app.ui.team.create.teaminfo.InsertTeamInfoFragment;

import dagger.Component;

/**
 * Created by tee on 16. 6. 24..
 */

@Component(modules = InsertTeamInfoModule.class)
public interface InsertTeamInfoComponent {
    void inject(InsertTeamInfoFragment fragment);
}
