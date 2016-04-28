package com.tosslab.jandi.app.ui.entities.chats.dagger;

import com.tosslab.jandi.app.ui.entities.chats.view.ChatsChooseFragment;

import dagger.Component;

@Component(modules = ChatChooseModule.class)
public interface ChatChooseComponent {
    void inject(ChatsChooseFragment fragment);
}
