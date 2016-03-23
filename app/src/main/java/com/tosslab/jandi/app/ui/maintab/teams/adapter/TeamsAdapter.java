package com.tosslab.jandi.app.ui.maintab.teams.adapter;

import android.view.ViewGroup;

import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.maintab.teams.adapter.viewholder.TeamCreateViewHolder;
import com.tosslab.jandi.app.ui.maintab.teams.adapter.viewholder.TeamPendingViewHolder;
import com.tosslab.jandi.app.ui.maintab.teams.adapter.viewholder.TeamViewHolder;
import com.tosslab.jandi.app.ui.team.select.to.Team;

/**
 * Created by tonyjs on 16. 3. 22..
 */
public class TeamsAdapter extends MultiItemRecyclerAdapter {

    public static final int VIEW_TYPE_TEAM = 0;
    public static final int VIEW_TYPE_TEAM_PENDING = 1;
    public static final int VIEW_TYPE_TEAM_CREATE = 2;

    private OnRequestTeamCreateListener onRequestTeamCreateListener;
    private OnTeamClickListener onTeamClickListener;

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            default:
            case VIEW_TYPE_TEAM:
                return TeamViewHolder.newInstance(parent);
            case VIEW_TYPE_TEAM_PENDING:
                return TeamPendingViewHolder.newInstance(parent);
            case VIEW_TYPE_TEAM_CREATE:
                return TeamCreateViewHolder.newInstance(parent);
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        holder.itemView.setOnClickListener(v -> {
            int itemViewType = getItemViewType(position);
            if (itemViewType == VIEW_TYPE_TEAM_CREATE) {
                if (onRequestTeamCreateListener != null) {
                    onRequestTeamCreateListener.onRequestTeamCreate();
                }
            } else if(itemViewType == VIEW_TYPE_TEAM){
                if (onTeamClickListener == null) {
                    return;
                }

                onTeamClickListener.onTeamClick(getItem(position));
            }
        });
    }

    public void setOnRequestTeamCreateListener(OnRequestTeamCreateListener onRequestTeamCreateListener) {
        this.onRequestTeamCreateListener = onRequestTeamCreateListener;
    }

    public void setOnTeamClickListener(OnTeamClickListener onTeamClickListener) {
        this.onTeamClickListener = onTeamClickListener;
    }

    public interface OnRequestTeamCreateListener {
        void onRequestTeamCreate();
    }

    public interface OnTeamClickListener {
        void onTeamClick(Team team);
    }

}
