package com.tosslab.jandi.app.ui.invites.email.component;

import com.tosslab.jandi.app.ui.invites.email.InviteByEmailActivity;
import com.tosslab.jandi.app.ui.invites.email.module.InviteByEmailModule;

import dagger.Component;

/**
 * Created by tonyjs on 16. 3. 30..
 */
@Component(modules = {InviteByEmailModule.class})
public interface InviteByEmailComponent {
    void inject(InviteByEmailActivity activity);
}
