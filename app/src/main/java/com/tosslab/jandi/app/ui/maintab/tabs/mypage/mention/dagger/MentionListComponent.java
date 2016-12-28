package com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.dagger;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.MentionListFragment;

import dagger.Component;

@Component(modules = {ApiClientModule.class, MentionListModule.class})
public interface MentionListComponent {

    void inject(MentionListFragment fragment);

}
