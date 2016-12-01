package com.tosslab.jandi.app.ui.maintab.tabs.chat.dagger;


import com.tosslab.jandi.app.ui.maintab.tabs.chat.MainChatListFragment;

import dagger.Component;

@Component(modules = {MainChatListModule.class})
public interface MainChatListComponent {
    void inject(MainChatListFragment fragment);
}
