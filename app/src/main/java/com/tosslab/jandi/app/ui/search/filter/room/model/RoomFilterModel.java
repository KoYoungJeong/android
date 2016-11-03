package com.tosslab.jandi.app.ui.search.filter.room.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.DirectMessageRoom;
import com.tosslab.jandi.app.team.room.Room;
import com.tosslab.jandi.app.team.room.TopicFolder;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by tonyjs on 2016. 7. 29..
 */
public class RoomFilterModel {

    public List<TopicFolder> getTopicRoomsWithFolder() {
        return TeamInfoLoader.getInstance().getTopicFolders();
    }

    public List<User> getUserList() {
        return TeamInfoLoader.getInstance().getUserList();
    }

    public List<User> getSearchedDirectMessages(String query, List<User> initializedDirectMessages) {
        List<User> searchedDirectMessages = new ArrayList<>();
        if (initializedDirectMessages == null || initializedDirectMessages.isEmpty()) {
            return searchedDirectMessages;
        }

        Observable.from(initializedDirectMessages)
                .filter(member -> {
                    if (TextUtils.isEmpty(query)) {
                        return true;
                    }

                    return member.getName().toLowerCase().contains(query.toLowerCase());
                })
                .toSortedList((entity, entity2) -> {
                    if (entity.isBot()) {
                        return -1;
                    } else if (entity2.isBot()) {
                        return 1;
                    } else {
                        return entity.getName().toLowerCase()
                                .compareTo(entity2.getName().toLowerCase());
                    }
                })
                .collect(() -> searchedDirectMessages, List::addAll)
                .subscribe(it -> {}, Throwable::printStackTrace);

        return searchedDirectMessages;
    }

    public List<TopicRoom> getSearchedTopics(String query) {
        List<TopicRoom> topicRooms = TeamInfoLoader.getInstance().getTopicList();
        if (topicRooms == null || topicRooms.isEmpty()) {
            return new ArrayList<>();
        }

        List<TopicRoom> searchedTopics = new ArrayList<>();

        Observable.from(topicRooms)
                .filter(TopicRoom::isJoined)
                .filter(topic -> TextUtils.isEmpty(query)
                        || topic.getName().toLowerCase().contains(query.toLowerCase()))
                .toSortedList((lhs, rhs) -> StringCompareUtil.compare(lhs.getName(), rhs.getName()))
                .collect(() -> searchedTopics, List::addAll)
                .subscribe(it -> {}, Throwable::printStackTrace);

        return searchedTopics;
    }

    public List<TopicRoom> getUnfoldedTopics(List<TopicFolder> topicFolders) {

        List<TopicRoom> unFoldedTopics = new ArrayList<>();

        List<Long> foldedTopicIds = new ArrayList<>();
        Observable.from(topicFolders)
                .concatMap(folder -> Observable.from(folder.getRooms()))
                .collect(() -> foldedTopicIds, (topicIds, topicRoom) -> topicIds.add(topicRoom.getId()))
                .defaultIfEmpty(new ArrayList<>(0))
                .concatMap(ids ->
                        Observable.from(TeamInfoLoader.getInstance().getTopicList())
                                .filter(topicRoom ->
                                        topicRoom.isJoined() && !(ids.contains(topicRoom.getId()))))
                .sorted((lhs, rhs) -> {
                    if (lhs.isStarred() && rhs.isStarred()) {
                        return StringCompareUtil.compare(lhs.getName(), rhs.getName());

                    } else if (lhs.isStarred()) {
                        return -1;
                    } else if (rhs.isStarred()) {
                        return 1;
                    } else {
                        return StringCompareUtil.compare(lhs.getName(), rhs.getName());
                    }
                })
                .collect(() -> unFoldedTopics, List::add)
                .subscribe();

        return unFoldedTopics;
    }

    public Observable<Long> getRoomIdFromMemberIdObservable(long memberId) {
        return Observable.from(TeamInfoLoader.getInstance().getDirectMessageRooms())
                .takeFirst(room -> room.getCompanionId() == memberId)
                .map(DirectMessageRoom::getId)
                .firstOrDefault(-1L);
    }

    public long getUserIdFromRoomId(long roomId) {
        Room room = TeamInfoLoader.getInstance().getRoom(roomId);
        if (room == null || room.getId() <= 0 || !(room instanceof DirectMessageRoom)) {
            return -1L;
        }

        return Observable.from(room.getMembers())
                .takeFirst(memberId -> TeamInfoLoader.getInstance().getMyId() != memberId)
                .toBlocking()
                .firstOrDefault(-1L);
    }
}
