package com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.component;

import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.MentionListFragment;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.module.MentionListModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by tonyjs on 16. 3. 17..
 */
@Component(modules = MentionListModule.class)
@Singleton
public interface MentionListComponent {

    void inject(MentionListFragment fragment);

}
