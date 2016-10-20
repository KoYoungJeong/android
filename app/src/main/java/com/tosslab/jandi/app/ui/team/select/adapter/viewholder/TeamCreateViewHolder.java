package com.tosslab.jandi.app.ui.team.select.adapter.viewholder;

import android.view.View;

import com.tosslab.jandi.app.ui.team.select.to.Team;

/**
 * Created by tee on 2016. 9. 28..
 */

public class TeamCreateViewHolder extends MainTeamListViewHolder {

    private View itemView;
    private OnTeamCreateClickListener onTeamCreateClickListener;

    public TeamCreateViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
    }

    @Override
    public void bind(Team team) {
        if (onTeamCreateClickListener != null) {
            itemView.setOnClickListener(v -> {
                onTeamCreateClickListener.onClick();
            });
        }
    }

    public void setOnTeamCreateClickListener(OnTeamCreateClickListener teamCreateClickListener) {
        this.onTeamCreateClickListener = teamCreateClickListener;
    }

    public interface OnTeamCreateClickListener {
        void onClick();
    }

}