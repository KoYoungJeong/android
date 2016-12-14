package com.tosslab.jandi.app.ui.invites.emails.dagger;

import com.tosslab.jandi.app.ui.invites.emails.InviteEmailActivity;

import dagger.Component;

/**
 * Created by tee on 2016. 12. 12..
 */

@Component(modules = {InviteEmailModule.class})
public interface InviteEmailComponent {
    void inject(InviteEmailActivity activity);
}