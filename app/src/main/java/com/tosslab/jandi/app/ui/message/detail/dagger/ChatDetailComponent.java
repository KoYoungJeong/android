package com.tosslab.jandi.app.ui.message.detail.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.message.detail.view.ChatDetailFragment;

import dagger.Component;

@Component(modules = ApiClientModule.class)
public interface ChatDetailComponent {
    void inject(ChatDetailFragment fragment);
}
