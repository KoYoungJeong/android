package com.tosslab.jandi.app.ui.maintab.team.adapter;

import android.view.ViewGroup;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.maintab.team.adapter.viewholder.MemberViewHolder;
import com.tosslab.jandi.app.ui.maintab.team.adapter.viewholder.MemberCountViewHolder;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public class TeamMemberListAdapter extends MultiItemRecyclerAdapter {

    public static final int VIEW_TYPE_MEMBER_COUNT = 0;
    public static final int VIEW_TYPE_MEMBER = 1;

    private OnMemberClickListener onMemberClickListener;

    public void setOnMemberClickListener(OnMemberClickListener onMemberClickListener) {
        this.onMemberClickListener = onMemberClickListener;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_MEMBER_COUNT:
                return MemberCountViewHolder.newInstance(parent);
            default:
            case VIEW_TYPE_MEMBER:
                return MemberViewHolder.newInstance(parent);
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (getItemViewType(position) == VIEW_TYPE_MEMBER
                && onMemberClickListener != null) {
            holder.itemView.setOnClickListener(
                    v -> onMemberClickListener.onMemberClick(getItem(position)));
        }
    }

    public interface OnMemberClickListener {
        void onMemberClick(FormattedEntity member);
    }
}
