package com.tosslab.jandi.app.ui.message.v2.search.dagger;


import com.tosslab.jandi.app.ui.message.v2.search.presenter.MessageSearchListPresenter;
import com.tosslab.jandi.app.ui.message.v2.search.presenter.MessageSearchListPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class MessageSearchListModule {
    private final MessageSearchListPresenter.View view;

    public MessageSearchListModule(MessageSearchListPresenter.View view) {this.view = view;}

    @Provides
    MessageSearchListPresenter.View view() {
        return view;
    }

    @Provides
    MessageSearchListPresenter presenter(MessageSearchListPresenterImpl presenter) {
        return presenter;
    }
}
