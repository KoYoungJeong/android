package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter;


import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

public interface TeamMemberDataView {
    void refresh();

    void setOnItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener);
}
