package com.tosslab.jandi.app.ui.entities.chats.dagger;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.entities.chats.adapter.ChatChooseAdapter;
import com.tosslab.jandi.app.ui.entities.chats.adapter.ChatChooseAdapterDataView;
import com.tosslab.jandi.app.ui.entities.chats.model.ChatChooseModel;
import com.tosslab.jandi.app.ui.entities.chats.presenter.ChatChoosePresenter;
import com.tosslab.jandi.app.ui.entities.chats.presenter.ChatChoosePresenterImpl;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor_;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel_;

import dagger.Module;
import dagger.Provides;

@Module
public class ChatChooseModule {

    private final ChatChoosePresenter.View view;
    private final ChatChooseAdapter chatChooseAdapter;

    public ChatChooseModule(ChatChoosePresenter.View view, ChatChooseAdapter chatChooseAdapter) {
        this.view = view;
        this.chatChooseAdapter = chatChooseAdapter;
    }

    @Provides
    ChatChooseAdapterDataView provideChatChooseAdapterDataView() {
        return chatChooseAdapter;
    }

    @Provides
    ChatChoosePresenter providesChatChoosePresenter() {
        return new ChatChoosePresenterImpl(new ChatChooseModel(),
                TeamDomainInfoModel_.getInstance_(JandiApplication.getContext()),
                InvitationDialogExecutor_.getInstance_(JandiApplication.getContext()),
                view,
                chatChooseAdapter);
    }

    @Provides
    InputMethodManager provideInputMethodManager() {
        return (InputMethodManager) JandiApplication
                .getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
    }
}
