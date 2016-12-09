package com.tosslab.jandi.app.ui.members.dagger;


import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.members.MembersListActivity;

import dagger.Component;

@Component(modules = {ApiClientModule.class, MemberListModule.class})
public interface MemberListComponent {
    void inject(MembersListActivity activity);
}
