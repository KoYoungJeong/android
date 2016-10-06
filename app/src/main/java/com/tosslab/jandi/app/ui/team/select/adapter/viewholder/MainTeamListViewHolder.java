package com.tosslab.jandi.app.ui.team.select.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tosslab.jandi.app.ui.team.select.to.Team;

/**
 * Created by tee on 2016. 9. 28..
 */

public abstract class MainTeamListViewHolder extends RecyclerView.ViewHolder {

    public MainTeamListViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(Team team);
}
