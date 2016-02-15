package com.tosslab.jandi.app.ui.share.views.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tee on 15. 9. 16..
 */
public class ShareTeamsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Team> teams;

    private OnItemClickListener onItemClickListener;

    public ShareTeamsAdapter() {
        this.teams = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(JandiApplication.getContext())
                .inflate(R.layout.item_share_team_list, parent, false);

        TeamViewHolder viewHolder = new TeamViewHolder(itemView);

        viewHolder.ivTeamIcon = (ImageView) itemView.findViewById(R.id.iv_team_list_icon);
        viewHolder.tvTeamName = (TextView) itemView.findViewById(R.id.tv_team_list_name);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Team item = getItem(position);
        TeamViewHolder viewHolder = (TeamViewHolder) holder;

        if (item.isSelected()) {
            viewHolder.ivTeamIcon.setImageResource(R.drawable.share_icon_teamlist_active);
            viewHolder.tvTeamName.setTextColor(0xFF003658);
        } else {
            viewHolder.ivTeamIcon.setImageResource(R.drawable.share_icon_teamlist_inactive);
            viewHolder.tvTeamName.setTextColor(0xFF404040);
        }

        viewHolder.tvTeamName.setText(item.getName());

        viewHolder.itemView.setOnClickListener(v -> {
            onItemClickListener.onItemClick(item.getTeamId(), item.getName());
        });
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    public Team getItem(int position) {
        return teams.get(position);
    }

    public void setItems(List<Team> teams) {
        this.teams = teams;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(long teamId, String teamName);
    }

    class TeamViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivTeamIcon;
        private TextView tvTeamName;

        public TeamViewHolder(View itemView) {
            super(itemView);
        }
    }

}
