package com.tosslab.jandi.app.ui.maintab.team.filter.member.adapter;


import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

public interface TeamMemberDataView {
    void refresh();

    void setOnItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener);
}
