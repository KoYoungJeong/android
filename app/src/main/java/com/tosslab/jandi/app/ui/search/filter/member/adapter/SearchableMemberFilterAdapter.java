package com.tosslab.jandi.app.ui.search.filter.member.adapter;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.members.adapter.searchable.viewholder.EmptySearchedMemberViewHolder;
import com.tosslab.jandi.app.ui.members.adapter.searchable.viewholder.MemberViewHolder;
import com.tosslab.jandi.app.ui.search.filter.member.adapter.model.MemberFilterableDataModel;
import com.tosslab.jandi.app.ui.search.filter.member.adapter.vieholder.AllMemberViewHolder;
import com.tosslab.jandi.app.ui.search.filter.member.adapter.view.MemberFilterableDataView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public class SearchableMemberFilterAdapter extends MultiItemRecyclerAdapter
        implements MemberFilterableDataModel, MemberFilterableDataView {

    public static final int VIEW_TYPE_ALL_MEMBER = 0;
    public static final int VIEW_TYPE_MEMBER = 1;
    public static final int VIEW_TYPE_EMPTY_QUERY = 2;

    private List<User> initializedMembers;

    private OnMemberClickListener onMemberClickListener;
    private OnAllMemberClickListener onAllMemberClickListener;
    private long selectedMemberId;

    public void setOnMemberClickListener(OnMemberClickListener onMemberClickListener) {
        this.onMemberClickListener = onMemberClickListener;
    }

    public void setOnAllMemberClickListener(OnAllMemberClickListener onAllMemberClickListener) {
        this.onAllMemberClickListener = onAllMemberClickListener;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_EMPTY_QUERY:
                return EmptySearchedMemberViewHolder.newInstance(parent);
            case VIEW_TYPE_ALL_MEMBER:
                return AllMemberViewHolder.newInstance(parent);
            default:
            case VIEW_TYPE_MEMBER:
                return MemberViewHolder.newInstance(parent);
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        int itemViewType = getItemViewType(position);

        View itemView = holder.itemView;
        if (itemViewType == VIEW_TYPE_ALL_MEMBER && onAllMemberClickListener != null) {
            itemView.setOnClickListener(v -> onAllMemberClickListener.onAllMemberClick());
        }

        if (itemViewType == VIEW_TYPE_MEMBER) {
            final User user = getItem(position);
            if (onMemberClickListener != null) {
                itemView.setOnClickListener(
                        v -> onMemberClickListener.onMemberClick(user));
            }

            boolean isSelectedMember = user.getId() == selectedMemberId;
            Resources resources = itemView.getResources();
            itemView.setBackgroundColor(isSelectedMember
                    ? resources.getColor(R.color.jandi_selected_member)
                    : resources.getColor(R.color.white));
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
        if (members == null || members.isEmpty()) {
            return;
        }

        List<Row<?>> rows = new ArrayList<>();

        if (members.size() == initializedMembers.size()) {
            rows.add(Row.create("", SearchableMemberFilterAdapter.VIEW_TYPE_ALL_MEMBER));
        }

        Observable.from(members)
                .map(member ->
                        Row.create(member, SearchableMemberFilterAdapter.VIEW_TYPE_MEMBER))
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
