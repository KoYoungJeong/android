package com.tosslab.jandi.app.ui.team.select.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.team.select.to.Team;

/**
 * Created by tee on 2016. 9. 28..
 */

public class TeamPendingViewHolder extends MainTeamListViewHolder {

    private TextView tvTeamListName;
    private TextView tvTeamListIgnoreButton;
    private TextView tvTeamListAcceptButton;

    private OnClickTeamJoinAcceptListener onClickTeamJoinAcceptListener;
    private OnClickTeamJoinIgnoreListener onClickTeamJoinIgnoreListener;

    public TeamPendingViewHolder(View itemView) {
        super(itemView);
        tvTeamListName = (TextView) itemView.findViewById(R.id.tv_team_list_name);
        tvTeamListIgnoreButton = (TextView) itemView.findViewById(R.id.tv_team_list_ignore_button);
        tvTeamListAcceptButton = (TextView) itemView.findViewById(R.id.tv_team_list_accept_button);
    }

    @Override
    public void bind(Team team) {
        tvTeamListName.setText(team.getName());

        if (onClickTeamJoinAcceptListener != null) {
            tvTeamListAcceptButton.setOnClickListener(v ->
                    onClickTeamJoinAcceptListener.onClick(team));
        }

        if (onClickTeamJoinIgnoreListener != null) {
            tvTeamListIgnoreButton.setOnClickListener(v ->
                    onClickTeamJoinIgnoreListener.onClick(team));
        }
    }

    public void setOnClickTeamJoinAcceptListener(OnClickTeamJoinAcceptListener onClickTeamJoinAcceptListener) {
        this.onClickTeamJoinAcceptListener = onClickTeamJoinAcceptListener;
    }

    public void setOnClickTeamJoinIgnoreListener(OnClickTeamJoinIgnoreListener onClickTeamJoinIgnoreListener) {
        this.onClickTeamJoinIgnoreListener = onClickTeamJoinIgnoreListener;
    }

    public interface OnClickTeamJoinAcceptListener {
        void onClick(Team team);
    }

    public interface OnClickTeamJoinIgnoreListener {
        void onClick(Team team);
    }
}
