package com.tosslab.jandi.app.ui.members.search.component;

import com.tosslab.jandi.app.ui.members.search.MemberSearchActivity;
import com.tosslab.jandi.app.ui.members.search.module.MemberSearchModule;

import dagger.Component;

/**
 * Created by tonyjs on 16. 4. 7..
 */
@Component(modules = {MemberSearchModule.class})
public interface MemberSearchComponent {
    void inject(MemberSearchActivity activity);
}
