package com.tosslab.jandi.app.ui.invites.emails.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.invites.emails.InviteEmailActivity;

import dagger.Component;

@Component(modules = {ApiClientModule.class, InviteEmailModule.class})
public interface InviteEmailComponent {
    void inject(InviteEmailActivity activity);
}