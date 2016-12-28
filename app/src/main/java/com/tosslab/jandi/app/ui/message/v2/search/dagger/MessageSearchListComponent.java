package com.tosslab.jandi.app.ui.message.v2.search.dagger;


import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.message.v2.search.view.MessageSearchListFragment;

import dagger.Component;

@Component(modules = {ApiClientModule.class, MessageSearchListModule.class})
public interface MessageSearchListComponent {
    void inject(MessageSearchListFragment fragment);
}
