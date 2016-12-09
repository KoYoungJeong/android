package com.tosslab.jandi.app.ui.members.dagger;


import com.tosslab.jandi.app.ui.members.presenter.MembersListPresenter;
import com.tosslab.jandi.app.ui.members.presenter.MembersListPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class MemberListModule {

    private final MembersListPresenter.View view;

    public MemberListModule(MembersListPresenter.View view) {
        this.view = view;
    }

    @Provides
    MembersListPresenter presenter(MembersListPresenterImpl presenter) {
        return presenter;
    }

    @Provides
    MembersListPresenter.View view() {
        return view;
    }


}
