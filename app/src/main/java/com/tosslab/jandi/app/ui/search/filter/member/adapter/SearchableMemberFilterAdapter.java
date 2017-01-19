package com.tosslab.jandi.app.ui.search.filter.member.adapter;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.members.adapter.searchable.viewholder.MemberViewHolder;
import com.tosslab.jandi.app.ui.search.filter.member.adapter.model.MemberFilterableDataModel;
import com.tosslab.jandi.app.ui.search.filter.member.adapter.vieholder.AllMemberViewHolder;
import com.tosslab.jandi.app.ui.search.filter.member.adapter.vieholder.EmptyMemberViewHolder;
import com.tosslab.jandi.app.ui.search.filter.member.adapter.view.MemberFilterableDataView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class SearchableMemberFilterAdapter extends MultiItemRecyclerAdapter
        implements MemberFilterableDataModel, MemberFilterableDataView {

    public static final int VIEW_TYPE_ALL_MEMBER = 0;
    public static final int VIEW_TYPE_MEMBER = 1;
    public static final int VIEW_TYPE_EMPTY_QUERY = 2;

    private List<User> initializedMembers;

    private OnMemberClickListener onMemberClickListener;
    private OnAllMemberClickListener onAllMemberClickListener;
    private long selectedMemberId;

    @Override
    public void setOnMemberClickListener(OnMemberClickListener onMemberClickListener) {
        this.onMemberClickListener = onMemberClickListener;
    }

    @Override
    public void setOnAllMemberClickListener(OnAllMemberClickListener onAllMemberClickListener) {
        this.onAllMemberClickListener = onAllMemberClickListener;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_EMPTY_QUERY:
                return EmptyMemberViewHolder.newInstance(parent);
            case VIEW_TYPE_ALL_MEMBER:
                return AllMemberViewHolder.newInstance(parent);
            default:
            case VIEW_TYPE_MEMBER:
                MemberViewHolder viewHolder = MemberViewHolder.createForChatChooseItem(parent);
                viewHolder.setIsTeamMemberList(true);
                return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        int itemViewType = getItemViewType(position);

        if (itemViewType == VIEW_TYPE_EMPTY_QUERY) {
            return;
        }

        View itemView = holder.itemView;
        if (itemViewType == VIEW_TYPE_ALL_MEMBER && onAllMemberClickListener != null) {
            itemView.setOnClickListener(v -> onAllMemberClickListener.onAllMemberClick());
        }

        if (itemViewType == VIEW_TYPE_MEMBER) {
            final ChatChooseItem user = getItem(position);
            if (onMemberClickListener != null) {
                itemView.setOnClickListener(
                        v -> onMemberClickListener.onMemberClick(user));
            }

            boolean isSelectedMember = user.getEntityId() == selectedMemberId;
            Resources resources = itemView.getResources();
            itemView.setBackgroundColor(isSelectedMember
                    ? resources.getColor(R.color.jandi_selected_member)
                    : resources.getColor(R.color.white));

            if (holder instanceof MemberViewHolder) {
                MemberViewHolder memberViewHolder = (MemberViewHolder) holder;
                if (position == getItemCount() - 1) {
                    memberViewHolder.showFullDivider();
                } else {
                    memberViewHolder.showHalfDivider();
                }
            }
        }

    }

    @Override
    public List<User> getInitializedMembers() {
        return initializedMembers;
    }

    @Override
    public synchronized void setInitializedMembers(List<User> currentMembers) {
        this.initializedMembers = currentMembers;
    }

    @Override
    public synchronized void addAll(List<User> members, long myId) {

        List<Row<?>> rows = new ArrayList<>();

        if (members == null || members.isEmpty()) {
            rows.add(Row.create("", SearchableMemberFilterAdapter.VIEW_TYPE_EMPTY_QUERY));
            return;
        }

        if (members.size() == initializedMembers.size()) {
            rows.add(Row.create("", SearchableMemberFilterAdapter.VIEW_TYPE_ALL_MEMBER));
        }

        Observable.from(members)
                .map(member ->
                        Row.create(ChatChooseItem.create(member).myId(member.getId() == myId),
                                SearchableMemberFilterAdapter.VIEW_TYPE_MEMBER))
                .subscribe(rows::add, Throwable::printStackTrace, () -> addRows(rows));
    }

    @Override
    public synchronized void clear() {
        super.clear();
    }

    @Override
    public void setSelectedMemberId(long selectedMemberId) {
        this.selectedMemberId = selectedMemberId;
    }

}
