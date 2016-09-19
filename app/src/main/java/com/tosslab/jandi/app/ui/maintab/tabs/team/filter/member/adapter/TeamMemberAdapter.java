package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter;


import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.domain.TeamDisabledMemberItem;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.domain.TeamMemberItem;
import com.tosslab.jandi.app.ui.members.adapter.searchable.viewholder.MemberViewHolder;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.Bind;

public class TeamMemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements TeamMemberDataModel, TeamMemberDataView, ToggleCollector {

    private static final int TYPE_DISABLED = 2;
    private static final int TYPE_NORMAL = 1;
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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_NORMAL) {
            return UserViewHolder.createForUser(parent);
        } else {
            return DisabledViewHolder.create(parent);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof TeamDisabledMemberItem) {
            return TYPE_DISABLED;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == TYPE_NORMAL) {
            UserViewHolder holder = (UserViewHolder) viewHolder;
            holder.setProfileImageClickable(false);
            holder.setSelectMode(isSelectedMode);
            TeamMemberItem item = getItem(position);
            item.getChatChooseItem().setIsChooseItem(containsId(item.getChatChooseItem().getEntityId()));
            holder.onBindView(item);
            if (!hasHeader) {
                if (position >= getItemCount() - 1) {
                    holder.showFullDivider();
                } else {
                    holder.showHalfDivider();
                }
            } else {
                if (isSameFirstCharacterToNext(position)) {
                    if (position >= getItemCount() - 1) {
                        holder.showFullDivider();
                    } else {
                        holder.showHalfDivider();
                    }
                } else {
                    if (isSameStarredToNext(position)) {
                        holder.showHalfDivider();
                    } else {
                        holder.showFullDivider();
                    }
                }
            }

        }
        viewHolder.itemView.setOnClickListener(v -> {
            if (onRecyclerItemClickListener != null) {
                onRecyclerItemClickListener.onItemClick(viewHolder.itemView, TeamMemberAdapter.this, position);
            }
        });

    }

    private boolean isSameStarredToNext(int position) {
        if (position >= getItemCount() - 1) {
            return false;
        } else {
            return getItem(position).getChatChooseItem().isStarred()
                    && getItem(position + 1).getChatChooseItem().isStarred();
        }
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


    static class DisabledViewHolder extends RecyclerView.ViewHolder {

        public DisabledViewHolder(View itemView) {
            super(itemView);
        }

        public static DisabledViewHolder create(ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.item_team_disabled_member, parent, false);
            return new DisabledViewHolder(view);
        }
    }

    static class UserViewHolder extends MemberViewHolder<TeamMemberItem> {

        @Bind(R.id.tv_user_name)
        TextView tvName;

        @Bind(R.id.cb_user_selected)
        AppCompatCheckBox cbUserSelected;

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
            setIsTeamMemberList(true);
            bindView(item.getChatChooseItem());
            cbUserSelected.setFocusable(false);
            cbUserSelected.setFocusableInTouchMode(false);
            cbUserSelected.setClickable(false);
            cbUserSelected.setOnClickListener(null);
            tvName.setText(item.getNameOfSpan(), TextView.BufferType.SPANNABLE);
        }
    }
}
