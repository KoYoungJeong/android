package com.tosslab.jandi.app.ui.entities.chats.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.entities.chats.view.ChatsChooseFragment;

import dagger.Component;

@Component(modules = {ApiClientModule.class, ChatChooseModule.class})
public interface ChatChooseComponent {
    void inject(ChatsChooseFragment fragment);
}
