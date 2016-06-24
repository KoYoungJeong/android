package com.tosslab.jandi.app.ui.poll.create.component;

import com.tosslab.jandi.app.ui.poll.create.PollCreateActivity;
import com.tosslab.jandi.app.ui.poll.create.module.PollCreateModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by tonyjs on 16. 6. 20..
 */
@Component(modules = PollCreateModule.class)
@Singleton
public interface PollCreateComponent {

    void inject(PollCreateActivity activity);

}
