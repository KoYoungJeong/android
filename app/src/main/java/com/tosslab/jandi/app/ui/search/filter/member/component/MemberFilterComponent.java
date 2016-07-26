package com.tosslab.jandi.app.ui.search.filter.member.component;

import com.tosslab.jandi.app.ui.search.filter.member.MemberFilterActivity;
import com.tosslab.jandi.app.ui.search.filter.member.module.MemberFilterModule;

import dagger.Component;

/**
 * Created by tonyjs on 16. 4. 7..
 */
@Component(modules = {MemberFilterModule.class})
public interface MemberFilterComponent {
    void inject(MemberFilterActivity activity);
}
