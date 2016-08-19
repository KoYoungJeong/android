package com.tosslab.jandi.app.ui.maintab.team.filter.dept.dagger;


import com.tosslab.jandi.app.ui.maintab.team.filter.dept.DeptJobFragment;

import dagger.Component;

@Component(modules = DeptJobModule.class)
public interface DeptJobComponent {
    void inject(DeptJobFragment fragment);
}
