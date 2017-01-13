package com.tosslab.jandi.app.ui.search.filter.room.adapter.model;

import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicFolder;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;

import java.util.List;

/**
 * Created by tonyjs on 2016. 7. 28..
 */
public interface RoomFilterDataModel {

    void addRows(List<MultiItemRecyclerAdapter.Row<?>> rows);

    List<MultiItemRecyclerAdapter.Row<?>> getTopicWithFolderRows(List<TopicFolder> folders);

    List<MultiItemRecyclerAdapter.Row<?>> getTopicRows(List<TopicRoom> topicRooms);

    List<MultiItemRecyclerAdapter.Row<?>> getUserRows(List<User> users, long myId);

    void setFolders(List<TopicFolder> topicFolders);

    List<TopicFolder> getTopicFolders();

    void setTopicRooms(List<TopicRoom> topicRooms);

    List<TopicRoom> getTopicRooms();

    void setUsers(List<User> users);

    List<User> getUsers();

    void clearAllRows();

    void setSelectedUserId(long userId);

    void setSelectedTopicRoomId(long topicRoomId);
}
