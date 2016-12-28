package com.tosslab.jandi.app.ui.poll.participants.component;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.poll.participants.PollParticipantsActivity;
import com.tosslab.jandi.app.ui.poll.participants.module.PollParticipantsModule;

import dagger.Component;

@Component(modules = {ApiClientModule.class, PollParticipantsModule.class})
public interface PollParticipantsComponent {
    void inject(PollParticipantsActivity activity);
}
