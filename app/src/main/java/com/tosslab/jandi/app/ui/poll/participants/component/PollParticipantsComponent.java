package com.tosslab.jandi.app.ui.poll.participants.component;

import com.tosslab.jandi.app.ui.poll.participants.PollParticipantsActivity;
import com.tosslab.jandi.app.ui.poll.participants.module.PollParticipantsModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by tonyjs on 16. 6. 27..
 */
@Component(modules = PollParticipantsModule.class)
@Singleton
public interface PollParticipantsComponent {
    void inject(PollParticipantsActivity activity);
}
