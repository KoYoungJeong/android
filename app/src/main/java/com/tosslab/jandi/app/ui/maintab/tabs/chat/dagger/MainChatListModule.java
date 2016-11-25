package com.tosslab.jandi.app.ui.maintab.tabs.chat.dagger;


import com.tosslab.jandi.app.ui.maintab.tabs.chat.model.MainChatListModel;
import com.tosslab.jandi.app.ui.maintab.tabs.chat.presenter.MainChatListPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.chat.presenter.MainChatListPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class MainChatListModule {
    private final MainChatListPresenter.View view;

    public MainChatListModule(MainChatListPresenter.View view) {
        this.view = view;}

    @Provides
    MainChatListPresenter.View view() {
        return view;
    }

    @Provides
    MainChatListPresenter presenter(MainChatListPresenterImpl presenter) {
        return presenter;
    }

    @Provides
    MainChatListModel model() {
        return new MainChatListModel();
    }
}
