package com.tosslab.jandi.app.ui.share.multi.dagger;

import com.tosslab.jandi.app.ui.share.multi.MultiShareFragment;

import dagger.Component;

@Component(modules = MultiShareModule.class)
public interface MultiShareComponent {
    void inject(MultiShareFragment fragment);
}
