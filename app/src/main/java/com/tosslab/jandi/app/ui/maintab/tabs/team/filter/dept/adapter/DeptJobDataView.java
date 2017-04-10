package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.adapter;


import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

public interface DeptJobDataView {
    void refresh();

    int getItemCount();

    void setOnItemClick(OnRecyclerItemClickListener onRecyclerItemClickListener);
}
