package com.tosslab.jandi.app.ui.maintab.tabs.topic.model;

import android.support.v4.util.LongSparseArray;
import android.support.v4.util.Pair;

import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.teams.folder.FolderApi;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.room.TopicFolder;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicFolderData;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicFolderListDataProvider;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicItemData;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
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
        return TeamInfoLoader.getInstance().getTopicFolders();
    }

    // Join된 Topic에 관한 정보를 가져오기
    private LongSparseArray<Topic> getJoinEntities(List<TopicRoom> topicRooms) {

        LongSparseArray<Topic> topicHashMap = new LongSparseArray<>();

        Observable.from(topicRooms)
                .map(topicRoom -> new Topic.Builder()
                        .entityId(topicRoom.getId())
                        .description(topicRoom.getDescription())
                        .isJoined(true)
                        .isPublic(topicRoom.isPublicTopic())
                        .isStarred(topicRoom.isStarred())
                        .memberCount(topicRoom.getMemberCount())
                        .name(topicRoom.getName())
                        .unreadCount(topicRoom.getUnreadCount())
                        .markerLinkId(topicRoom.getLastLinkId())
                        .isPushOn(topicRoom.isPushSubscribe())
                        .readOnly(topicRoom.isReadOnly())
                        .build())
                .collect(() -> topicHashMap, (array, topic) -> array.put(topic.getEntityId(), topic))
                .subscribe(_1 -> {}, Throwable::printStackTrace);

        return topicHashMap;

    }

    // 리스트에 보여 줄 Data Provider 가져오기
    public TopicFolderListDataProvider getDataProvider(List<TopicFolder> topicFolders, List<TopicRoom> topicRooms) {
        if (topicFolders == null || topicRooms == null) {
            return new TopicFolderListDataProvider(new LinkedList<>());
        }

        final List<TopicFolder> orderedFolders = new ArrayList<>();

        Observable.from(topicFolders)
                .toSortedList((lhs, rhs) -> lhs.getSeq() - rhs.getSeq())
                .subscribe(orderedFolders::addAll, Throwable::printStackTrace);

        List<Pair<TopicFolderData,
                List<TopicItemData>>> datas = new LinkedList<>();

        LongSparseArray<Topic> joinTopics = getJoinEntities(topicRooms);

        long folderIndex = 0;

        LongSparseArray<List<TopicItemData>> topicItemMap = new LongSparseArray<>();
        LongSparseArray<TopicFolderData> folderMap = new LongSparseArray<>();
        LongSparseArray<Integer> badgeCountMap = new LongSparseArray<>();

        for (TopicFolder topicFolder : orderedFolders) {
            if (topicItemMap.indexOfKey(topicFolder.getId()) < 0) {
                topicItemMap.put(topicFolder.getId(), new ArrayList<>());
            }
            if (badgeCountMap.indexOfKey(topicFolder.getId()) < 0) {
                badgeCountMap.put(topicFolder.getId(), 0);
            }
            if (folderMap.indexOfKey(topicFolder.getId()) < 0) {
                TopicFolderData topicFolderData = new TopicFolderData(folderIndex, topicFolder.getName(), topicFolder.getId());
                topicFolderData.setSeq(topicFolder.getSeq());
                folderMap.put(topicFolder.getId(), topicFolderData);
            }
            folderIndex++;
        }

        Observable.from(topicFolders)
                .subscribe(topicFolder -> {
                    Observable.from(topicFolder.getRooms())
                            .filter(topicRoom -> joinTopics.indexOfKey(topicRoom.getId()) >= 0)
                            .subscribe(topicRoom -> {

                                Topic topic = joinTopics.get(topicRoom.getId());
                                joinTopics.remove(topicRoom.getId());
                                long itemIndex = folderMap.get(topicFolder.getId()).generateNewChildId();

                                TopicItemData topicItemData = TopicItemData.newInstance(
                                        itemIndex, topic.getCreatorId(), topic.getName(),
                                        topic.isStarred(), topic.isJoined(), topic.getEntityId(),
                                        topic.getUnreadCount(), topic.getMarkerLinkId(), topic.isPushOn(),
                                        topic.isSelected(), topic.getDescription(), topic.isPublic(),
                                        topic.getMemberCount(), topic.isReadOnly());
                                topicItemMap.get(topicFolder.getId()).add(topicItemData);

                                int badgeCount = badgeCountMap.get(topicFolder.getId());
                                badgeCountMap.put(topicFolder.getId(), badgeCount + topicItemData
                                        .getUnreadCount());
                            });

                }, Throwable::printStackTrace);

        int size = folderMap.size();
        for (int idx = 0; idx < size; idx++) {
            long folderId = folderMap.keyAt(idx);
            List<TopicItemData> topicItemDatas = topicItemMap.get(folderId);
            List<TopicItemData> providerTopicItemDatas = new ArrayList<>();

            Collections.sort(topicItemDatas, (lhs, rhs) -> {
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

            providerTopicItemDatas.addAll(topicItemDatas);

            TopicFolderData topicFolderData = folderMap.get(folderId);
            topicFolderData.setItemCount(topicItemDatas.size());
            topicFolderData.setChildBadgeCnt(badgeCountMap.get(folderId));

            datas.add(new Pair<>(topicFolderData, providerTopicItemDatas));
        }

        folderIndex = folderMap.size();

        // 폴더가 없는 토픽 데이터 셋팅
        TopicFolderData fakeFolder = getFakeFolder(folderIndex);
        List<TopicItemData> noFolderTopicItemDatas = new ArrayList<>();
        Observable.range(0, joinTopics.size())
                .map(joinTopics::keyAt)
                .map(topicId -> {
                    long itemIndex = fakeFolder.generateNewChildId();
                    Topic topic = joinTopics.get(topicId);
                    return TopicItemData.newInstance(
                            itemIndex, topic.getCreatorId(), topic.getName(),
                            topic.isStarred(), topic.isJoined(), topic.getEntityId(),
                            topic.getUnreadCount(), topic.getMarkerLinkId(), topic.isPushOn(),
                            topic.isSelected(), topic.getDescription(), topic.isPublic(), topic.getMemberCount(), topic.isReadOnly());
                })
                .subscribe(noFolderTopicItemDatas::add);

        // Topic join button을 위한 더미 인스턴스 추가
        if (TeamInfoLoader.getInstance().getMyLevel() != Level.Guest) {
            noFolderTopicItemDatas.add(TopicItemData.getDummyInstance());
        }
        datas.add(new Pair<>(fakeFolder, noFolderTopicItemDatas));
        return new TopicFolderListDataProvider(datas);
    }

    // 그룹이 없는 Topic 들을 담아낼 더미 그룹 생성
    public TopicFolderData getFakeFolder(long lastFolderIndex) {
        TopicFolderData topicFolderData = new TopicFolderData(lastFolderIndex, "fakeFolder", -1);
        topicFolderData.setIsFakeFolder(true);
        return topicFolderData;
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

    public Observable<Integer> getUnreadCount() {

        return Observable.from(TeamInfoLoader.getInstance().getTopicList())
                .filter(TopicRoom::isJoined)
                .map(TopicRoom::getUnreadCount)
                .reduce((unreadCount1, unreadCount2) -> unreadCount1 + unreadCount2);

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
}