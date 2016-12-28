package com.tosslab.jandi.app.ui.message.v2.dagger;


import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Fragment;

import dagger.Component;

@Component(modules = {ApiClientModule.class, MessageListModule.class})
public interface MessageListComponent {
    void inject(MessageListV2Fragment fragment);
}
