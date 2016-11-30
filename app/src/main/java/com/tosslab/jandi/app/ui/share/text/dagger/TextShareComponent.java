package com.tosslab.jandi.app.ui.share.text.dagger;


import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.share.text.TextShareFragment;

import dagger.Component;

@Component(modules = {TextShareModule.class, ApiClientModule.class})
public interface TextShareComponent {
    void inject(TextShareFragment fragment);
}
