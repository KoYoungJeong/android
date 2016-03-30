package com.tosslab.jandi.app.ui.invites.email.module;

import com.tosslab.jandi.app.ui.invites.email.adapter.InvitedEmailListAdapter;
import com.tosslab.jandi.app.ui.invites.email.model.InviteByEmailModel;
import com.tosslab.jandi.app.ui.invites.email.model.InvitedEmailDataModel;
import com.tosslab.jandi.app.ui.invites.email.presenter.InviteByEmailPresenter;
import com.tosslab.jandi.app.ui.invites.email.presenter.InviteByEmailPresenterImpl;
import com.tosslab.jandi.app.ui.invites.email.view.InviteByEmailView;
import com.tosslab.jandi.app.ui.invites.email.view.InvitedEmailView;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 16. 3. 30..
 */
@Module
public class InviteByEmailModule {

    private final InviteByEmailView inviteByEmailView;
    private final InvitedEmailDataModel invitedEmailDataModel;
    public InviteByEmailModule(InviteByEmailView inviteByEmailView,
                               InvitedEmailDataModel invitedEmailDataModel) {
        this.inviteByEmailView = inviteByEmailView;
        this.invitedEmailDataModel = invitedEmailDataModel;
    }

    @Provides
    public InviteByEmailView provideInviteByEmailView() {
        return inviteByEmailView;
    }

    @Provides
    public InviteByEmailModel provideInviteByEmailModel() {
        return new InviteByEmailModel();
    }

    @Provides
    public InvitedEmailDataModel provideInvitedEmailDataModel() {
        return invitedEmailDataModel;
    }

    @Provides
    public InviteByEmailPresenter provideInviteByEmailPresenter(InviteByEmailPresenterImpl presenter) {
        return presenter;
    }

}
