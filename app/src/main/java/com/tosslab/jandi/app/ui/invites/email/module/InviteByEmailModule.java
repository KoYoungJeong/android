package com.tosslab.jandi.app.ui.invites.email.module;

import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.invites.email.adapter.InvitedEmailListAdapter;
import com.tosslab.jandi.app.ui.invites.email.model.InviteByEmailModel;
import com.tosslab.jandi.app.ui.invites.email.model.InvitedEmailDataModel;
import com.tosslab.jandi.app.ui.invites.email.presenter.InviteByEmailPresenter;
import com.tosslab.jandi.app.ui.invites.email.presenter.InviteByEmailPresenterImpl;
import com.tosslab.jandi.app.ui.invites.email.view.InviteByEmailView;
import com.tosslab.jandi.app.ui.invites.email.view.InvitedEmailView;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tonyjs on 16. 3. 30..
 */
@Module(includes = ApiClientModule.class)
public class InviteByEmailModule {

    private final InviteByEmailView inviteByEmailView;
    private final InvitedEmailListAdapter invitedEmailListAdapter;

    public InviteByEmailModule(InviteByEmailView inviteByEmailView,
                               InvitedEmailListAdapter invitedEmailListAdapter) {
        this.inviteByEmailView = inviteByEmailView;
        this.invitedEmailListAdapter = invitedEmailListAdapter;
    }

    @Provides
    public InviteByEmailView provideInviteByEmailView() {
        return inviteByEmailView;
    }

    @Provides
    public InviteByEmailModel provideInviteByEmailModel(Lazy<TeamApi> teamApi) {
        return new InviteByEmailModel(teamApi);
    }

    @Provides
    public InvitedEmailView provideInvitedEmailView() {
        return invitedEmailListAdapter;
    }

    @Provides
    public InvitedEmailDataModel provideInvitedEmailDataModel() {
        return invitedEmailListAdapter;
    }

    @Provides
    public InviteByEmailPresenter provideInviteByEmailPresenter(InviteByEmailPresenterImpl presenter) {
        return presenter;
    }

}