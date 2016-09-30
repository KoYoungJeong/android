package com.tosslab.jandi.app.ui.team.select.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.team.select.to.Team;

/**
 * Created by tee on 2016. 9. 28..
 */

public class TeamItemViewHolder extends MainTeamListViewHolder {

    private View itemView;
    private TextView tvTeamName;
    private TextView tvTeamMyEmail;
    private TextView tvTeamBadge;

    private OnTeamItemClickListener onTeamItemClickListener;

    public TeamItemViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        tvTeamName = (TextView) itemView.findViewById(R.id.tv_team_name);
        tvTeamMyEmail = (TextView) itemView.findViewById(R.id.tv_team_my_email);
        tvTeamBadge = (TextView) itemView.findViewById(R.id.tv_team_badge);
    }

    @Override
    public void bind(Team team) {
        tvTeamName.setText(team.getName());
        tvTeamMyEmail.setText(team.getUserEmail());
        if (team.getUnread() > 0) {
            tvTeamBadge.setVisibility(View.VISIBLE);
            tvTeamBadge.setText(String.valueOf(team.getUnread()));
        } else {
            tvTeamBadge.setVisibility(View.GONE);
        }
        if (onTeamItemClickListener != null) {
            itemView.setOnClickListener(v -> onTeamItemClickListener.onClick(team.getTeamId()));
        }
    }

    public void setOnTeamItemClickListener(OnTeamItemClickListener teamItemClickListener) {
        this.onTeamItemClickListener = teamItemClickListener;
    }

    public interface OnTeamItemClickListener {
        void onClick(long teamId);
    }
}