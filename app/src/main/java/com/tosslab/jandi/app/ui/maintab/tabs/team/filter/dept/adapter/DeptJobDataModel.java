package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.adapter;


import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.domain.DeptJob;

import java.util.List;

public interface DeptJobDataModel {
    DeptJob getItem(int position);

    int getSize();

    void add(DeptJob item);

    void addAll(List<DeptJob> items);

    void clear();
}
