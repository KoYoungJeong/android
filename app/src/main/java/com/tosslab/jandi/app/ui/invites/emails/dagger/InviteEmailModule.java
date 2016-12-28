package com.tosslab.jandi.app.ui.invites.emails.dagger;

import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.ui.invites.emails.adapter.InviteEmailListAdapter;
import com.tosslab.jandi.app.ui.invites.emails.adapter.InviteEmailListAdapterDataModel;
import com.tosslab.jandi.app.ui.invites.emails.adapter.InviteEmailListAdapterViewModel;
import com.tosslab.jandi.app.ui.invites.emails.model.InviteEmailModel;
import com.tosslab.jandi.app.ui.invites.emails.presenter.InviteEmailPresenter;
import com.tosslab.jandi.app.ui.invites.emails.presenter.InviteEmailPresenterImpl;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

@Module
public class InviteEmailModule {
    private InviteEmailPresenter.View view;
    private InviteEmailListAdapter listAdapter;

    public InviteEmailModule(InviteEmailPresenter.View view, InviteEmailListAdapter listAdapter) {
        this.view = view;
        this.listAdapter = listAdapter;
    }


    @Provides
    public InviteEmailListAdapterDataModel providesAdapterDataModel() {
        return listAdapter;
    }

    @Provides
    public InviteEmailListAdapterViewModel providesAdapterViewModel() {
        return listAdapter;
    }

    @Provides
    public InviteEmailPresenter.View provideViewInPresenter() {
        return view;
    }

    @Provides
    public InviteEmailPresenter provideInviteEmailPresenter(InviteEmailPresenterImpl presenter) {
        return presenter;
    }

    @Provides
    public InviteEmailModel provideInviteEmailModel(Lazy<TeamApi> teamApi) {
        return new InviteEmailModel(teamApi);
    }

}