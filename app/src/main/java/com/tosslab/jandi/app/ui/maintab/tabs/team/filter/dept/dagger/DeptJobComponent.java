package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.dagger;


import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.DeptJobFragment;

import dagger.Component;

@Component(modules = DeptJobModule.class)
public interface DeptJobComponent {
    void inject(DeptJobFragment fragment);
}
