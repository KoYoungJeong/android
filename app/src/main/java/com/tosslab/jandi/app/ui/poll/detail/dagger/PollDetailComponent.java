package com.tosslab.jandi.app.ui.poll.detail.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.poll.detail.PollDetailActivity;

import dagger.Component;

@Component(modules = {ApiClientModule.class, PollDetailModule.class})
public interface PollDetailComponent {

    void inject(PollDetailActivity activity);

}
