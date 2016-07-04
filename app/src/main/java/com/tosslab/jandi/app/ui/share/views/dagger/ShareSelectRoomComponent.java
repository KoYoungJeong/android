package com.tosslab.jandi.app.ui.share.views.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.share.views.ShareSelectTeamActivity;

import dagger.Component;

@Component(modules = ApiClientModule.class)
public interface ShareSelectRoomComponent {
    void inject(ShareSelectTeamActivity activity);
}
