package com.tosslab.jandi.app.ui.poll.create.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.poll.create.PollCreateActivity;

import dagger.Component;

@Component(modules = {ApiClientModule.class, PollCreateModule.class})
public interface PollCreateComponent {

    void inject(PollCreateActivity activity);

}
