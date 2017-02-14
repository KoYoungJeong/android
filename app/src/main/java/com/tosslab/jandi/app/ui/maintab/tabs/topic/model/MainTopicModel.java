package com.tosslab.jandi.app.ui.maintab.tabs.topic.model;

import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.teams.folder.FolderApi;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicFolder;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.IMarkerTopicFolderItem;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicFolderData;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicItemData;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;
import rx.functions.Func0;

public class MainTopicModel {

    EntityClientManager entityClientManager;

    Lazy<FolderApi> folderApi;

    @Inject
    public MainTopicModel(EntityClientManager entityClientManager, Lazy<FolderApi> folderApi) {
        this.entityClientManager = entityClientManager;
        this.folderApi = folderApi;
    }

    // 폴더 정보 가져오기
    public List<TopicFolder> getTopicFolders() {
        List<TopicFolder> topicFolders = new ArrayList<>();
        Observable.from(TeamInfoLoader.getInstance().getTopicFolders())
                .sorted((lhs, rhs) -> lhs.getSeq() - rhs.getSeq())
                .collect(() -> topicFolders, List::add)
                .subscribe();
        return topicFolders;
    }

    public Observable<List<Topic>> getUpdatedTopicList() {

        return Observable.from(TeamInfoLoader.getInstance().getTopicList())
                .filter(TopicRoom::isJoined)
                .map(topicRoom -> new Topic.Builder()
                        .name(topicRoom.getName())
                        .isStarred(topicRoom.isStarred())
                        .isJoined(true)
                        .entityId(topicRoom.getId())
                        .memberCount(topicRoom.getMemberCount())
                        .unreadCount(topicRoom.getUnreadCount())
                        .isPublic(topicRoom.isPublicTopic())
                        .description(topicRoom.getDescription())
                        .creatorId(topicRoom.getCreatorId())
                        .markerLinkId(topicRoom.getReadLinkId())
                        .lastLinkId(topicRoom.getLastLinkId())
                        .isPushOn(topicRoom.isPushSubscribe())
                        .readOnly(topicRoom.isReadOnly())
                        .build())
                .toSortedList((lhs, rhs) -> {
                    long lhsLastLinkId = lhs.getLastLinkId();
                    long rhsLastLinkId = rhs.getLastLinkId();
                    if (lhsLastLinkId > rhsLastLinkId) {
                        return -1;
                    } else if (lhsLastLinkId < rhsLastLinkId) {
                        return 1;
                    } else {
                        return 0;
                    }
                });
    }

    public long findFolderId(long topicId) {
        return Observable.from(TeamInfoLoader.getInstance().getTopicFolders())
                .takeFirst(topicFolder -> topicFolder.getRooms().contains(topicId))
                .map(TopicFolder::getId)
                .toBlocking()
                .firstOrDefault(-1L);
    }

    public List<TopicRoom> getJoinedTopics() {
        return Observable.from(TeamInfoLoader.getInstance().getTopicList())
                .filter(TopicRoom::isJoined)
                .collect((Func0<ArrayList<TopicRoom>>) ArrayList::new, ArrayList::add)
                .toBlocking()
                .firstOrDefault(new ArrayList<>());

    }

    public List<IMarkerTopicFolderItem> getTopicFolderDatas() {

        List<TopicFolder> topicFolders = new ArrayList<>();
        topicFolders.addAll(getTopicFolders());

        List<TopicRoom> joinedTopics = getJoinedTopics();
        LinkedHashMap<Long, TopicRoom> joinedTopicsHashMap = new LinkedHashMap<>();

        for (TopicRoom topicRoom : joinedTopics) {
            joinedTopicsHashMap.put(topicRoom.getId(), topicRoom);
        }

        for (TopicFolder topicFolder : topicFolders) {
            for (TopicRoom topicRoom : topicFolder.getRooms()) {
                joinedTopicsHashMap.remove(topicRoom.getId());
            }
        }

        TopicFolder dummyFolder = TopicFolder.makeDummyFolder();

        List<TopicRoom> noFolderTopics = new ArrayList<>();

        for (Long id : joinedTopicsHashMap.keySet()) {
            noFolderTopics.add(joinedTopicsHashMap.get(id));
        }

        if (noFolderTopics.size() > 0) {
            dummyFolder.setRooms(noFolderTopics);
            topicFolders.add(dummyFolder);
        }

        List<IMarkerTopicFolderItem> TopicFolderItems = new ArrayList<>();

        Observable.from(topicFolders)
                .map(topicFolder -> {
                    if (!topicFolder.isDummy()) {
                        TopicFolderData topicFolderData = new TopicFolderData();
                        topicFolderData.setFolderId(topicFolder.getId());
                        topicFolderData.setItemCount(topicFolder.getRooms().size());
                        topicFolderData.setSeq(topicFolder.getSeq());
                        topicFolderData.setTitle(topicFolder.getName());
                        long childBadgeCnt = 0;
                        for (TopicRoom room : topicFolder.getRooms()) {
                            childBadgeCnt += room.getUnreadCount();
                        }
                        topicFolderData.setChildBadgeCnt(childBadgeCnt);
                        TopicFolderItems.add(topicFolderData);
                    }

                    int index = -1;

                    Collections.sort(topicFolder.getRooms(), (lhs, rhs) -> {
                        if (lhs.isStarred() && rhs.isStarred()) {
                            return StringCompareUtil.compare(lhs.getName(), rhs.getName());
                        } else if (lhs.isStarred()) {
                            return -1;
                        } else if (rhs.isStarred()) {
                            return 1;
                        } else {
                            return StringCompareUtil.compare(lhs.getName(), rhs.getName());
                        }
                    });

                    for (TopicRoom room : topicFolder.getRooms()) {
                        index++;
                        TopicItemData topicItemData = new TopicItemData();
                        topicItemData.setName(room.getName());
                        topicItemData.setStarred(room.isStarred());
                        topicItemData.setJoined(true);
                        topicItemData.setEntityId(room.getId());
                        topicItemData.setMemberCount(room.getMemberCount());
                        topicItemData.setUnreadCount(room.getUnreadCount());
                        topicItemData.setPublic(room.getType().equals("channel"));
                        topicItemData.setDescription(room.getDescription());
                        topicItemData.setCreatorId(room.getCreatorId());
                        topicItemData.setMarkerLinkId(room.getReadLinkId());
                        topicItemData.setPushOn(room.isPushSubscribe());
                        topicItemData.setReadOnly(room.isReadOnly());
                        topicItemData.setChildIndex(index);
                        topicItemData.setParentChildCnt(topicFolder.getRooms().size());
                        topicItemData.setInnerFolder(!topicFolder.isDummy());
                        if (!topicFolder.isDummy()) {
                            topicItemData.setParentId(topicFolder.getId());
                        } else {
                            topicItemData.setParentId(-1);
                        }
                        TopicFolderItems.add(topicItemData);
                    }
                    return topicFolder;
                }).subscribe();

        return TopicFolderItems;
    }
}