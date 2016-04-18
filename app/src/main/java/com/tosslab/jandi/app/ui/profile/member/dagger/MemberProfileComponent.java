package com.tosslab.jandi.app.ui.profile.member.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;

import dagger.Component;

@Component(modules = ApiClientModule.class)
public interface MemberProfileComponent {
    void inject(MemberProfileActivity activity);
}
