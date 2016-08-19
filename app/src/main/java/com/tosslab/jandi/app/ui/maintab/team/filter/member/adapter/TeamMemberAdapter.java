package com.tosslab.jandi.app.ui.maintab.team.filter.member.adapter;


import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.domain.TeamMemberItem;
import com.tosslab.jandi.app.ui.members.adapter.searchable.viewholder.MemberViewHolder;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class TeamMemberAdapter extends RecyclerView.Adapter<TeamMemberAdapter.UserViewHolder>
        implements TeamMemberDataModel, TeamMemberDataView {

    private List<TeamMemberItem> users;
    private OnRecyclerItemClickListener onRecyclerItemClickListener;
    private boolean isSelectedMode;

    public TeamMemberAdapter() {
        users = new ArrayList<>();
    }

    @Override
    public void add(TeamMemberItem user) {
        users.add(user);
    }

    @Override
    public void addAll(List<TeamMemberItem> users) {
        this.users.addAll(users);
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return UserViewHolder.createForUser(parent);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        holder.setSelectMode(isSelectedMode);
        holder.onBindView(getItem(position));
        if (isSelectedMode) {
            holder.showFullDivider();
        } else {
            if (isSameFirstCharacterToNext(position)) {
                holder.showHalfDivider();
            } else {
                holder.dismissDividers();
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (onRecyclerItemClickListener != null) {
                onRecyclerItemClickListener.onItemClick(holder.itemView, TeamMemberAdapter.this, position);
            }
        });
    }

    private boolean isSameFirstCharacterToNext(int position) {
        if (position >= getItemCount() - 1) {
            return true;
        } else {
            return TextUtils.equals(getItem(position).getFirstCharacter(), getItem(position + 1).getFirstCharacter());
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public TeamMemberItem getItem(int position) {
        return users.get(position);
    }

    @Override
    public int getSize() {
        return getItemCount();
    }

    @Override
    public void clear() {
        users.clear();
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @Override
    public void refresh() {
        notifyDataSetChanged();
    }

    @Override
    public void setOnItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    public void setSelectedMode(boolean selectedMode) {
        isSelectedMode = selectedMode;
    }


    static class UserViewHolder extends MemberViewHolder<TeamMemberItem> {

        UserViewHolder(View itemView) {
            super(itemView);
        }

        public static UserViewHolder createForUser(ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.item_entity_body_two_line, parent, false);
            return new UserViewHolder(itemView);
        }

        @Override
        public void onBindView(TeamMemberItem item) {
            bindView(item.getChatChooseItem());
            setIsTeamMemberList(true);
        }
    }
}
