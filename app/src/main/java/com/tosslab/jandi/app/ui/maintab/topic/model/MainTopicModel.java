package com.tosslab.jandi.app.ui.maintab.topic.model;

import android.support.v4.util.Pair;

import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.teams.folder.FolderApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicFolder;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicFolderData;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicFolderListDataProvider;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicItemData;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;
import rx.functions.Func0;

@EBean
public class MainTopicModel {

    @Bean
    EntityClientManager entityClientManager;

    @Inject
    Lazy<FolderApi> folderApi;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create()
                .inject(this);
    }

    // 폴더 정보 가져오기
    public List<TopicFolder> getTopicFolders() {
        return TeamInfoLoader.getInstance().getTopicFolders();
    }

    // Join된 Topic에 관한 정보를 가져오기
    private LinkedHashMap<Long, Topic> getJoinEntities(List<TopicRoom> topicRooms) {

        LinkedHashMap<Long, Topic> topicHashMap = new LinkedHashMap<>();

        Observable.from(topicRooms)
                .map(formattedEntity -> new Topic.Builder()
                        .entityId(formattedEntity.getId())
                        .description(formattedEntity.getDescription())
                        .isJoined(true)
                        .isPublic(formattedEntity.isPublicTopic())
                        .isStarred(formattedEntity.isStarred())
                        .memberCount(formattedEntity.getMemberCount())
                        .name(formattedEntity.getName())
                        .unreadCount(formattedEntity.getUnreadCount())
                        .markerLinkId(formattedEntity.getLastLinkId())
                        .isPushOn(formattedEntity.isPushSubscribe())
                        .build())
                .toSortedList((lhs, rhs) -> {
                    if (lhs.isStarred() && rhs.isStarred()) {
                        return StringCompareUtil.compare(lhs.getName(), rhs.getName());
                    } else if (lhs.isStarred()) {
                        return -1;
                    } else if (rhs.isStarred()) {
                        return 1;
                    } else {
                        return StringCompareUtil.compare(lhs.getName(), rhs.getName());
                    }

                }).subscribe(topics -> {
            for (Topic topic : topics) {
                topicHashMap.put(topic.getEntityId(), topic);
            }
        });

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
                .subscribe(orderedFolders::addAll);

        List<Pair<TopicFolderData,
                List<TopicItemData>>> datas = new LinkedList<>();

        LinkedHashMap<Long, Topic> joinTopics = getJoinEntities(topicRooms);

        long folderIndex = 0;

        Map<Long, List<TopicItemData>> topicItemMap = new HashMap<>();
        Map<Long, TopicFolderData> folderMap = new LinkedHashMap<>();
        Map<Long, Integer> badgeCountMap = new HashMap<>();

        for (TopicFolder topicFolder : orderedFolders) {
            if (!topicItemMap.containsKey(topicFolder.getId())) {
                topicItemMap.put(topicFolder.getId(), new ArrayList<>());
            }
            if (!badgeCountMap.containsKey(topicFolder.getId())) {
                badgeCountMap.put(topicFolder.getId(), 0);
            }
            if (!folderMap.containsKey(topicFolder.getId())) {
                TopicFolderData topicFolderData = new TopicFolderData(folderIndex, topicFolder.getName(), topicFolder.getId());
                topicFolderData.setSeq(topicFolder.getSeq());
                folderMap.put(topicFolder.getId(), topicFolderData);
            }
            folderIndex++;
        }

        Observable.from(topicFolders)
                .subscribe(topicFolder -> {

                    Observable.from(topicFolder.getRooms())
                            .filter(topicRoom -> joinTopics.containsKey(topicRoom.getId()))
                            .subscribe(topicRoom -> {

                                Topic topic = joinTopics.remove(topicRoom.getId());
                                long itemIndex = folderMap.get(topicFolder.getId()).generateNewChildId();

                                TopicItemData topicItemData = TopicItemData.newInstance(
                                        itemIndex, topic.getCreatorId(), topic.getName(),
                                        topic.isStarred(), topic.isJoined(), topic.getEntityId(),
                                        topic.getUnreadCount(), topic.getMarkerLinkId(), topic.isPushOn(),
                                        topic.isSelected(), topic.getDescription(), topic.isPublic(),
                                        topic.getMemberCount());
                                topicItemMap.get(topicFolder.getId()).add(topicItemData);

                                int badgeCount = badgeCountMap.get(topicFolder.getId());
                                badgeCountMap.put(topicFolder.getId(), badgeCount + topicItemData
                                        .getUnreadCount());
                            });

                }, Throwable::printStackTrace);

        for (Long folderId : folderMap.keySet()) {

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
        Observable.from(joinTopics.keySet())
                .map(topicId -> {
                    long itemIndex = fakeFolder.generateNewChildId();
                    Topic topic = joinTopics.get(topicId);
                    return TopicItemData.newInstance(
                            itemIndex, topic.getCreatorId(), topic.getName(),
                            topic.isStarred(), topic.isJoined(), topic.getEntityId(),
                            topic.getUnreadCount(), topic.getMarkerLinkId(), topic.isPushOn(),
                            topic.isSelected(), topic.getDescription(), topic.isPublic(), topic.getMemberCount());
                })
                .subscribe(noFolderTopicItemDatas::add);

        // Topic join button을 위한 더미 인스턴스 추가
        noFolderTopicItemDatas.add(TopicItemData.getDummyInstance());
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
                .scan((unreadCount1, unreadCount2) -> unreadCount1 + unreadCount2);

    }

    public long findFolderId(long topicId) {
        return Observable.from(TeamInfoLoader.getInstance().getTopicFolders())
                .takeFirst(topicFolder -> topicFolder.getRooms().contains(topicId))
                .map(TopicFolder::getId)
                .toBlocking()
                .firstOrDefault(-1l);
    }

    public List<TopicRoom> getJoinedTopics() {
        return Observable.from(TeamInfoLoader.getInstance().getTopicList())
                .filter(TopicRoom::isJoined)
                .collect((Func0<ArrayList<TopicRoom>>) ArrayList::new, ArrayList::add)
                .toBlocking()
                .firstOrDefault(new ArrayList<>());

    }
}