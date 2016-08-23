package com.tosslab.jandi.app.ui.members.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.members.adapter.searchable.viewholder.MemberViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ModdableMemberListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int OWNER_TYPE_TEAM = 0;
    public static final int OWNER_TYPE_TOPIC = 1;

    private List<ChatChooseItem> memberChooseItems;
    private boolean isCheckMode = false;
    private boolean isKickMode;
    private int ownerType = OWNER_TYPE_TEAM;
    private OnKickClickListener onKickClickListener;
    private OnMemberClickListener onMemberClickListener;

    public ModdableMemberListAdapter(int ownerType) {
        this.ownerType = ownerType;
        memberChooseItems = new ArrayList<>();
    }

    public void setOnMemberClickListener(OnMemberClickListener onMemberClickListener) {
        this.onMemberClickListener = onMemberClickListener;
    }

    public int getCount() {
        return memberChooseItems.size();
    }

    public ChatChooseItem getItem(int position) {
        return memberChooseItems.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return MemberViewHolder.createForChatChooseItem(parent);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ChatChooseItem item = getItem(position);

        MemberViewHolder viewHolder = (MemberViewHolder) holder;

        if (ownerType == OWNER_TYPE_TEAM) {
            viewHolder.setIsTeamMemberList(true);
        } else {
            viewHolder.setIsTeamMemberList(false);
        }

        if (isKickMode) {
            viewHolder.setKickMode(true);
            viewHolder.setSelectMode(false);
            viewHolder.setKickClickListener(v -> {
                if (onKickClickListener != null) {
                    onKickClickListener.onKickClick(ModdableMemberListAdapter.this, holder, position);
                }
            });
        } else if (isCheckMode) {
            viewHolder.setSelectMode(true);
            viewHolder.setProfileImageClickable(true);
            viewHolder.setKickMode(false);
            viewHolder.setKickClickListener(null);
        } else {
            viewHolder.setSelectMode(false);
            viewHolder.setProfileImageClickable(false);
            viewHolder.setKickMode(false);
            viewHolder.setKickClickListener(null);
        }

        viewHolder.onBindView(item);

        if (onMemberClickListener != null) {
            holder.itemView.setOnClickListener((v) -> onMemberClickListener.onMemberClick(item));
        }

        if (holder instanceof MemberViewHolder) {
            MemberViewHolder memberViewHolder = (MemberViewHolder) holder;
            if (position == getItemCount() - 1) {
                memberViewHolder.showFullDivider();
            } else {
                memberViewHolder.showHalfDivider();
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return memberChooseItems.size();
    }

    public void addAll(List<ChatChooseItem> memberList) {
        memberChooseItems.addAll(memberList);
    }

    public void clear() {
        memberChooseItems.clear();
    }

    public void setCheckMode() {
        isCheckMode = true;
    }

    public List<Long> getSelectedUserIds() {
        List<Long> selectedUserIds = new ArrayList<>();
        for (ChatChooseItem item : memberChooseItems) {
            if (item.isChooseItem()) {
                selectedUserIds.add(item.getEntityId());
            }
        }
        return selectedUserIds;
    }

    public void setKickMode(boolean kickMode) {
        this.isKickMode = kickMode;
    }

    public void setOnKickClickListener(OnKickClickListener onKickClickListener) {
        this.onKickClickListener = onKickClickListener;
    }

    public void remove(int position) {
        memberChooseItems.remove(position);
    }

    public interface OnMemberClickListener {
        void onMemberClick(ChatChooseItem item);
    }

    public interface OnKickClickListener {
        void onKickClick(RecyclerView.Adapter adapter, RecyclerView.ViewHolder viewHolder, int position);
    }

}
