package com.tosslab.jandi.app.ui.maintab.team.filter.member.adapter;


import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.domain.TeamMemberItem;
import com.tosslab.jandi.app.ui.members.adapter.searchable.viewholder.MemberViewHolder;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeamMemberAdapter extends RecyclerView.Adapter<TeamMemberAdapter.UserViewHolder>
        implements TeamMemberDataModel, TeamMemberDataView, ToggleCollector {

    private List<TeamMemberItem> users;
    private OnRecyclerItemClickListener onRecyclerItemClickListener;
    private boolean isSelectedMode;
    private boolean hasHeader;

    private Set<Long> toggledIds;

    public TeamMemberAdapter() {
        users = new ArrayList<>();
        toggledIds = new HashSet<>();
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
        TeamMemberItem item = getItem(position);
        item.getChatChooseItem().setIsChooseItem(containsId(item.getChatChooseItem().getEntityId()));
        holder.onBindView(item);
        if (!hasHeader) {
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
    public int findItemOfEntityId(long userId) {
        for (int idx = 0; idx < users.size(); idx++) {
            TeamMemberItem user = users.get(idx);
            if (user.getChatChooseItem().getEntityId() == userId) {
                return idx;
            }
        }
        return -1;
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

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    @Override
    public boolean containsId(long id) {
        return toggledIds.contains(id);
    }

    @Override
    public void addId(long id) {
        toggledIds.add(id);
    }

    @Override
    public void addAllIds(List<Long> ids) {
        toggledIds.addAll(ids);
    }

    @Override
    public void removeId(long id) {
        toggledIds.remove(id);
    }

    @Override
    public void clearIds() {
        toggledIds.clear();
    }

    @Override
    public int count() {
        return toggledIds.size();
    }

    @Override
    public List<Long> getIds() {
        return Collections.unmodifiableList(new ArrayList<>(toggledIds));
    }


    static class UserViewHolder extends MemberViewHolder<TeamMemberItem> {

        UserViewHolder(View itemView) {
            super(itemView);
        }

        public static UserViewHolder createForUser(ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.item_entity_body_two_line, parent, false);
            TypedValue typedValue = new TypedValue();
            itemView.getContext().getTheme()
                    .resolveAttribute(R.attr.selectableItemBackground, typedValue, true);
            itemView.setBackgroundResource(typedValue.resourceId);

            return new UserViewHolder(itemView);
        }

        @Override
        public void onBindView(TeamMemberItem item) {
            bindView(item.getChatChooseItem());
            setIsTeamMemberList(true);
        }
    }
}
