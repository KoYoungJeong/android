package com.tosslab.jandi.app.ui.members.adapter.searchable;

import android.view.ViewGroup;

import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.members.adapter.searchable.viewholder.EmptySearchedMemberViewHolder;
import com.tosslab.jandi.app.ui.members.adapter.searchable.viewholder.MemberCountViewHolder;
import com.tosslab.jandi.app.ui.members.adapter.searchable.viewholder.MemberViewHolder;
import com.tosslab.jandi.app.ui.members.model.MemberSearchableDataModel;
import com.tosslab.jandi.app.ui.members.view.MemberSearchableDataView;

import java.util.List;

import rx.Observable;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public class SearchableMemberListAdapter extends MultiItemRecyclerAdapter
        implements MemberSearchableDataModel, MemberSearchableDataView {

    public static final int VIEW_TYPE_MEMBER_COUNT = 0;
    public static final int VIEW_TYPE_MEMBER = 1;
    public static final int VIEW_TYPE_EMPTY_QUERY = 2;

    private OnMemberClickListener onMemberClickListener;
    private List<User> initializedMembers;

    public void setOnMemberClickListener(OnMemberClickListener onMemberClickListener) {
        this.onMemberClickListener = onMemberClickListener;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_EMPTY_QUERY:
                return EmptySearchedMemberViewHolder.newInstance(parent);
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

    @Override
    public List<User> getInitializedMembers() {
        return initializedMembers;
    }

    @Override
    public synchronized void setInitializedMembers(List<User> currentMembers) {
        this.initializedMembers = currentMembers;
    }

    @Override
    public synchronized void addAll(List<User> members) {
        MultiItemRecyclerAdapter.Row<Integer> memberCountRow =
                new MultiItemRecyclerAdapter.Row<>(members.size(),
                        SearchableMemberListAdapter.VIEW_TYPE_MEMBER_COUNT);
        Observable
                .concat(Observable.just(memberCountRow),
                        Observable.from(members)
                                .map(entity ->
                                        new MultiItemRecyclerAdapter.Row<User>(
                                                entity, SearchableMemberListAdapter.VIEW_TYPE_MEMBER)))
                .subscribe(this::addRow, Throwable::printStackTrace);
    }

    @Override
    public void setEmptySearchedMember(String query) {
        setRow(0, new MultiItemRecyclerAdapter.Row<>(
                query, SearchableMemberListAdapter.VIEW_TYPE_EMPTY_QUERY));
    }

    @Override
    public synchronized void clear() {
        super.clear();
    }
}
