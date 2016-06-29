package com.tosslab.jandi.app.ui.poll.list.component;

import com.tosslab.jandi.app.ui.poll.list.PollListActivity;
import com.tosslab.jandi.app.ui.poll.list.module.PollListModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by tonyjs on 16. 6. 28..
 */
@Component(modules = PollListModule.class)
@Singleton
public interface PollListComponent {
    void inject(PollListActivity activity);
}
