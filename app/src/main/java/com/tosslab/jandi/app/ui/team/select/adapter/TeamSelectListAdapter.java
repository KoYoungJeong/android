package com.tosslab.jandi.app.ui.team.select.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.team.select.adapter.datamodel.TeamSelectListAdapterDataModel;
import com.tosslab.jandi.app.ui.team.select.adapter.viewholder.MainTeamListViewHolder;
import com.tosslab.jandi.app.ui.team.select.adapter.viewholder.TeamCreateViewHolder;
import com.tosslab.jandi.app.ui.team.select.adapter.viewholder.TeamItemViewHolder;
import com.tosslab.jandi.app.ui.team.select.adapter.viewholder.TeamPendingViewHolder;
import com.tosslab.jandi.app.ui.team.select.adapter.viewmodel.TeamSelectListAdapterViewModel;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tee on 2016. 9. 28..
 */

public class TeamSelectListAdapter extends RecyclerView.Adapter<MainTeamListViewHolder> implements
        TeamSelectListAdapterDataModel, TeamSelectListAdapterViewModel {

    private final int TYPE_CREATE = 0x01;
    private final int TYPE_JOINED = 0x02;
    private final int TYPE_PENDING = 0x03;

    private List<Team> teams = new ArrayList<>();

    private TeamCreateViewHolder.OnTeamCreateClickListener onTeamCreateClickListener;
    private TeamItemViewHolder.OnTeamItemClickListener onTeamItemClickListener;
    private TeamPendingViewHolder.OnClickTeamJoinIgnoreListener onClickTeamJoinIgnoreListener;
    private TeamPendingViewHolder.OnClickTeamJoinAcceptListener onClickTeamJoinAcceptListener;

    @Override
    public MainTeamListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_CREATE:
                itemView = inflater.inflate(R.layout.item_select_team_create, parent, false);
                TeamCreateViewHolder teamCreateViewHolder = new TeamCreateViewHolder(itemView);
                teamCreateViewHolder.setOnTeamCreateClickListener(onTeamCreateClickListener);
                return teamCreateViewHolder;
            case TYPE_JOINED:
                itemView = inflater.inflate(R.layout.item_select_team, parent, false);
                TeamItemViewHolder teamItemViewHolder = new TeamItemViewHolder(itemView);
                teamItemViewHolder.setOnTeamItemClickListener(onTeamItemClickListener);
                return teamItemViewHolder;
            case TYPE_PENDING:
                itemView = inflater.inflate(R.layout.item_select_team_pending, parent, false);
                TeamPendingViewHolder teamPendingViewHolder = new TeamPendingViewHolder(itemView);
                teamPendingViewHolder.setOnClickTeamJoinAcceptListener(onClickTeamJoinAcceptListener);
                teamPendingViewHolder.setOnClickTeamJoinIgnoreListener(onClickTeamJoinIgnoreListener);
                return teamPendingViewHolder;
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(MainTeamListViewHolder holder, int position) {
        holder.bind(getDatas().get(position));
    }

    @Override
    public int getItemViewType(int position) {
        Team.Status status = teams.get(position).getStatus();
        if (status.equals(Team.Status.CREATE)) {
            return TYPE_CREATE;
        } else if (status.equals(Team.Status.JOINED)) {
            return TYPE_JOINED;
        } else if (status.equals(Team.Status.PENDING)) {
            return TYPE_PENDING;
        }
        return TYPE_JOINED;
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    private List<Team> getDatas() {
        return teams;
    }

    @Override
    public void setDatas(List<Team> teams) {
        this.teams = teams;
    }

    @Override
    public void justRefresh() {
        notifyDataSetChanged();
    }

    @Override
    public void setOnTeamCreateClickListener(
            TeamCreateViewHolder.OnTeamCreateClickListener onTeamCreateClickListener) {
        this.onTeamCreateClickListener = onTeamCreateClickListener;
    }

    @Override
    public void setOnTeamItemClickListener(
            TeamItemViewHolder.OnTeamItemClickListener onTeamItemClickListener) {
        this.onTeamItemClickListener = onTeamItemClickListener;
    }

    @Override
    public void setOnClickTeamJoinIgnoreListener(
            TeamPendingViewHolder.OnClickTeamJoinIgnoreListener onClickTeamJoinIgnoreListener) {
        this.onClickTeamJoinIgnoreListener = onClickTeamJoinIgnoreListener;
    }

    @Override
    public void setOnClickTeamJoinAcceptListener(
            TeamPendingViewHolder.OnClickTeamJoinAcceptListener onClickTeamJoinAcceptListener) {
        this.onClickTeamJoinAcceptListener = onClickTeamJoinAcceptListener;
    }
}