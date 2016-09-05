package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.deptgroup.dagger;


import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.deptgroup.DeptJobGroupActivity;

import dagger.Component;

@Component(modules = DeptJobGroupModule.class)
public interface DeptJobGroupComponent {
    void inject(DeptJobGroupActivity activity);
}
