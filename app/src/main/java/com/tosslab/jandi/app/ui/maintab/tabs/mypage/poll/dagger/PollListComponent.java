package com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.PollListFragment;

import dagger.Component;

@Component(modules = {ApiClientModule.class, PollListModule.class})
public interface PollListComponent {
    void inject(PollListFragment fragment);
}
