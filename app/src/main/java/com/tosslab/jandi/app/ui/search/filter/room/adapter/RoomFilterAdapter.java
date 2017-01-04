package com.tosslab.jandi.app.ui.search.filter.room.adapter;

import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicFolder;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.members.adapter.searchable.viewholder.MemberViewHolder;
import com.tosslab.jandi.app.ui.search.filter.room.adapter.model.RoomFilterDataModel;
import com.tosslab.jandi.app.ui.search.filter.room.adapter.view.RoomFilterDataView;
import com.tosslab.jandi.app.ui.search.filter.room.adapter.viewholder.FolderViewHolder;
import com.tosslab.jandi.app.ui.search.filter.room.adapter.viewholder.TopicRoomViewHolder;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.views.decoration.DividerViewHolder;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by tonyjs on 2016. 7. 28..
 */
public class RoomFilterAdapter extends MultiItemRecyclerAdapter
        implements RoomFilterDataModel, RoomFilterDataView {

    private static final int VIEW_TYPE_FOLDER = 0;
    private static final int VIEW_TYPE_TOPIC = 1;
    private static final int VIEW_TYPE_USER = 2;
    private static final int VIEW_TYPE_FOLDER_DIVIDER = 3;

    private List<TopicFolder> topicFolders;
    private List<TopicRoom> topicRooms;
    private List<User> users;

    private OnTopicRoomClickListener onTopicRoomClickListener;
    private OnMemberClickListener onMemberClickListener;

    private long selectedUserId = -1L;
    private long selectedTopicRoomId = -1L;

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_FOLDER:
                return FolderViewHolder.newInstance(parent);
            case VIEW_TYPE_FOLDER_DIVIDER:
                return DividerViewHolder.newInstance(parent);
            case VIEW_TYPE_TOPIC:
                return TopicRoomViewHolder.newInstance(parent);
            case VIEW_TYPE_USER:
                MemberViewHolder memberViewHolder = MemberViewHolder.createForUser(parent);
                memberViewHolder.setIsTeamMemberList(true);
                return memberViewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        View itemView = holder.itemView;
        Resources resources = itemView.getResources();

        if (getItem(position) instanceof TopicRoom) {
            TopicRoom room = getItem(position);

            boolean isSelectedTopic = room.getId() == selectedTopicRoomId;
            itemView.setBackgroundColor(isSelectedTopic
                    ? resources.getColor(R.color.jandi_selected_member)
                    : resources.getColor(R.color.white));

            if (onTopicRoomClickListener != null) {
                itemView.setOnClickListener(v ->
                        onTopicRoomClickListener.onTopicRoomClick(room.getId()));
            }

        } else if (getItem(position) instanceof User) {
            User user = getItem(position);

            boolean isSelectedMember = user.getId() == selectedUserId;
            itemView.setBackgroundColor(isSelectedMember
                    ? resources.getColor(R.color.jandi_selected_member)
                    : resources.getColor(R.color.white));

            if (onMemberClickListener != null) {
                itemView.setOnClickListener(v ->
                        onMemberClickListener.onMemberClick(user.getId()));
            }
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
    public List<MultiItemRecyclerAdapter.Row<?>> getTopicWithFolderRows(List<TopicFolder> folders) {
        List<Row<?>> rows = new ArrayList<>();

        if (folders == null || folders.isEmpty()) {
            return rows;
        }

        Observable.from(folders)
                .toSortedList((folder, folder2) -> folder.getSeq() - folder2.getSeq())
                .concatMap(Observable::from)
                .doOnNext(folder -> {
                    if (rows.size() > 0) {
                        rows.addAll(getFolderDividerRow());
                    }
                    rows.add(Row.create(folder.getFolder(), VIEW_TYPE_FOLDER));
                })
                .concatMap(folder -> Observable.from(folder.getRooms()))
                .subscribe(topicRoom ->
                                rows.add(Row.create(topicRoom, VIEW_TYPE_TOPIC)),
                        Throwable::printStackTrace,
                        () -> {
                            rows.addAll(getFolderDividerRow());
                        });

        return rows;
    }

    private List<Row<?>> getFolderDividerRow() {
        List<Row<?>> rows = new ArrayList<>();

        DividerViewHolder.Info marginDividerInfo =
                DividerViewHolder.Info.create((int) UiUtils.getPixelFromDp(8), Color.TRANSPARENT);
        rows.add(Row.create(marginDividerInfo, VIEW_TYPE_FOLDER_DIVIDER));

        int height = (int) UiUtils.getPixelFromDp(1);
        int color = JandiApplication.getContext()
                .getResources().getColor(R.color.rgb_eeeeee);
        DividerViewHolder.Info dividerInfo =
                DividerViewHolder.Info.create(height, color);
        rows.add(Row.create(dividerInfo, VIEW_TYPE_FOLDER_DIVIDER));
        return rows;
    }

    @Override
    public List<MultiItemRecyclerAdapter.Row<?>> getTopicRows(List<TopicRoom> topicRooms) {
        List<Row<?>> rows = new ArrayList<>();

        if (topicRooms == null || topicRooms.isEmpty()) {
            return rows;
        }

        Observable.from(topicRooms)
                .subscribe(topicRoom -> rows.add(Row.create(topicRoom, VIEW_TYPE_TOPIC)));

        return rows;
    }

    @Override
    public List<Row<?>> getUserRows(List<User> users) {
        List<Row<?>> rows = new ArrayList<>();

        if (users == null || users.isEmpty()) {
            return rows;
        }

        Observable.from(users)
                .subscribe(user -> rows.add(Row.create(user, VIEW_TYPE_USER)));

        return rows;
    }

    @Override
    public void setFolders(List<TopicFolder> topicFolders) {
        this.topicFolders = topicFolders;
    }

    @Override
    public List<TopicFolder> getTopicFolders() {
        return this.topicFolders;
    }

    @Override
    public List<TopicRoom> getTopicRooms() {
        return this.topicRooms;
    }

    @Override
    public void setTopicRooms(List<TopicRoom> topicRooms) {
        this.topicRooms = topicRooms;
    }

    @Override
    public List<User> getUsers() {
        return this.users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    @Override
    public void clearAllRows() {
        clear();
    }

    @Override
    public void setSelectedUserId(long userId) {
        this.selectedUserId = userId;
    }

    @Override
    public void setSelectedTopicRoomId(long topicRoomId) {
        this.selectedTopicRoomId = topicRoomId;
    }

    @Override
    public void setOnTopicRoomClickListener(OnTopicRoomClickListener onTopicRoomClickListener) {
        this.onTopicRoomClickListener = onTopicRoomClickListener;
    }

    @Override
    public void setOnMemberClickListener(OnMemberClickListener onMemberClickListener) {
        this.onMemberClickListener = onMemberClickListener;
    }

}