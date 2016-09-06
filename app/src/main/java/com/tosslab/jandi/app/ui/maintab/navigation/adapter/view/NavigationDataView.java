package com.tosslab.jandi.app.ui.maintab.navigation.adapter.view;

import android.view.MenuItem;

import com.tosslab.jandi.app.ui.maintab.navigation.adapter.viewholder.TeamCreateViewHolder;
import com.tosslab.jandi.app.ui.team.select.to.Team;

/**
 * Created by tonyjs on 2016. 8. 17..
 */
public interface NavigationDataView {

    void notifyDataSetChanged();

    void setOnNavigationItemClickListener(OnNavigationItemClickListener onNavigationItemClickListener);

    void setOnTeamClickListener(OnTeamClickListener onTeamClickListener);

    void setOnRequestTeamCreateListener(TeamCreateViewHolder.OnRequestTeamCreateListener onRequestTeamCreateListener);

    void setOnVersionClickListener(OnVersionClickListener onVersionClickListener);

    interface OnTeamClickListener {
        void onTeamClick(Team team);
    }

    interface OnNavigationItemClickListener {
        void onNavigationItemClick(MenuItem menuItem);
    }

    interface OnVersionClickListener {
        void onVersionClick();
    }
}
