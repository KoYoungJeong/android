package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eowise.recyclerview.stickyheaders.StickyHeadersAdapter;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.domain.TeamDisabledMemberItem;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.domain.TeamMemberItem;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TeamMemberHeaderAdapter implements StickyHeadersAdapter<TeamMemberHeaderAdapter.HeaderViewHolder> {

    private static long DISABLE_MEMBER_ID_HEADER_ID = -0x111;
    private static long STARRED_MEMBER_ID_HEADER_ID = -0x112;

    private TeamMemberDataModel dataModel;

    public TeamMemberHeaderAdapter(TeamMemberDataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public HeaderViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_member_header, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HeaderViewHolder headerViewHolder, int position) {
        TeamMemberItem item = dataModel.getItem(position);

        if (item instanceof TeamDisabledMemberItem) {
            headerViewHolder.itemView.setVisibility(View.GONE);
        } else {
            headerViewHolder.itemView.setVisibility(View.VISIBLE);

            if (item.getChatChooseItem().isStarred()) {
                headerViewHolder.tvTitle.setText(R.string.jandi_starred_star);
            } else {
                headerViewHolder.tvTitle.setText(item.getFirstCharacter());
            }
        }


    }

    @Override
    public long getHeaderId(int position) {
        TeamMemberItem item = dataModel.getItem(position);
        if (item instanceof TeamDisabledMemberItem) {
            return DISABLE_MEMBER_ID_HEADER_ID;
        } else {
            if (item.getChatChooseItem().isStarred()) {
                return STARRED_MEMBER_ID_HEADER_ID;
            } else {
                return item.getFirstCharacter().hashCode();
            }
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_team_member_header_message)
        TextView tvTitle;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
