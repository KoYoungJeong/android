package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.dagger;

import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.adapter.DeptJobAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.adapter.DeptJobDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.adapter.DeptJobDataView;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.presenter.DeptJobPresenter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.presenter.DeptJobPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class DeptJobModule {
    private final DeptJobPresenter.View view;
    private final DeptJobAdapter adapter;
    private final int type;

    public DeptJobModule(DeptJobPresenter.View view, DeptJobAdapter adapter, int type) {
        this.view = view;
        this.adapter = adapter;
        this.type = type;
    }

    @Provides
    DeptJobPresenter.View view() {
        return view;
    }

    @Provides
    DeptJobDataModel deptJobDataModel() {
        return adapter;
    }


    @Provides
    DeptJobDataView deptJobDataView() {
        return adapter;
    }

    @Provides
    DeptJobPresenter deptJobPresenter(DeptJobPresenterImpl presenter) {
        presenter.setType(type);
        return presenter;
    }

}
