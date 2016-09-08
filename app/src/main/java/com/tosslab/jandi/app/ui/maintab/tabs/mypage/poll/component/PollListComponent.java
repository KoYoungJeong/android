package com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.component;

import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.PollListFragment;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.module.PollListModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by tonyjs on 16. 6. 28..
 */
@Component(modules = PollListModule.class)
@Singleton
public interface PollListComponent {
    void inject(PollListFragment fragment);
}
