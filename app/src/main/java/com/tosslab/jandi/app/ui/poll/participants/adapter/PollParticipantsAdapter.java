package com.tosslab.jandi.app.ui.poll.participants.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.members.adapter.searchable.viewholder.MemberViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tonyjs on 16. 6. 27..
 */
public class PollParticipantsAdapter extends MultiItemRecyclerAdapter {

    public static int VIEW_TYPE_TITLE = 0;
    public static int VIEW_TYPE_MEMBER = 1;
    private OnMemberClickListener onMemberClickListener;

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MEMBER) {
            return MemberViewHolder.createForUser(parent);
        } else if (viewType == VIEW_TYPE_TITLE) {
            return PollOptionTitleViewHolder.newInstance(parent);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (getItemViewType(position) == VIEW_TYPE_MEMBER
                && onMemberClickListener != null) {
            holder.itemView.setOnClickListener(
                    v -> onMemberClickListener.onMemberClick(getItem(position)));
        }

        if (holder instanceof MemberViewHolder) {
            MemberViewHolder memberViewHolder = (MemberViewHolder)holder;
            if (position == getItemCount() - 1) {
                memberViewHolder.showFullDivider();
            } else {
                memberViewHolder.showHalfDivider();
            }
        }
    }

    public void setOnMemberClickListener(OnMemberClickListener onMemberClickListener) {
        this.onMemberClickListener = onMemberClickListener;
    }

    public interface OnMemberClickListener {
        void onMemberClick(User member);
    }

    public static class PollOptionTitleViewHolder extends BaseViewHolder<String> {

        @Bind(R.id.tv_poll_participants_title)
        TextView tvTitle;

        private PollOptionTitleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public static PollOptionTitleViewHolder newInstance(ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.item_poll_participants_title, parent, false);
            return new PollOptionTitleViewHolder(itemView);
        }

        @Override
        public void onBindView(String title) {
            tvTitle.setText(title);
        }
    }

}
