package com.tosslab.jandi.app.ui.maintab.topic.model;

import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.TopicFolderRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.network.models.ResFolderItem;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicFolderData;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicFolderListDataProvider;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicItemData;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import retrofit.RetrofitError;
import rx.Observable;

@EBean
public class MainTopicModel {

    @Bean
    EntityClientManager entityClientManager;

    // 폴더 정보 가져오기
    public List<ResFolder> getTopicFolders() throws RetrofitError {
        if (!NetworkCheckUtil.isConnected()) {
            return TopicFolderRepository.getRepository().getFolders();
        }

        return RequestApiManager.getInstance()
                .getFoldersByTeamApi(entityClientManager.getSelectedTeamId());
    }

    // 폴더 속 토픽 아이디 가져오기
    public List<ResFolderItem> getTopicFolderItems() throws RetrofitError {
        if (!NetworkCheckUtil.isConnected()) {
            return TopicFolderRepository.getRepository().getFolderItems();
        }
        List<ResFolderItem> folderItems = RequestApiManager.getInstance()
                .getFolderItemsByTeamApi(entityClientManager.getSelectedTeamId());

        for (ResFolderItem resFolderItem : folderItems) {
            resFolderItem.teamId = entityClientManager.getSelectedTeamId();
        }

        return folderItems;
    }

    // Join된 Topic에 관한 정보를 가져오기
    public LinkedHashMap<Long, Topic> getJoinEntities() {

        EntityManager entityManager = EntityManager.getInstance();
        List<FormattedEntity> joinedChannels = entityManager.getJoinedChannels();
        List<FormattedEntity> groups = entityManager.getGroups();
        LinkedHashMap<Long, Topic> topicHashMap = new LinkedHashMap<>();

        Observable<Topic> observable = Observable.merge(Observable.from(joinedChannels), Observable.from(groups))
                .map(formattedEntity -> new Topic.Builder()
                        .entityId(formattedEntity.getId())
                        .description(formattedEntity.getDescription())
                        .isJoined(true)
                        .isPublic(formattedEntity.isPublicTopic())
                        .isStarred(formattedEntity.isStarred)
                        .memberCount(formattedEntity.getMemberCount())
                        .name(formattedEntity.getName())
                        .unreadCount(formattedEntity.alarmCount)
                        .markerLinkId(formattedEntity.lastLinkId)
                        .isPushOn(formattedEntity.isTopicPushOn)
                        .build());

        observable.toSortedList((lhs, rhs) -> {

            if (lhs.isStarred() && rhs.isStarred()) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            } else if (lhs.isStarred()) {
                return -1;
            } else if (rhs.isStarred()) {
                return 1;
            } else {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }

        }).subscribe(topics -> {
            for (Topic topic : topics) {
                topicHashMap.put(topic.getEntityId(), topic);
            }
        });

        return topicHashMap;

    }

    // 리스트에 보여 줄 Data Provider 가져오기
    public TopicFolderListDataProvider getDataProvider(List<ResFolder> topicFolders, List<ResFolderItem> topicFolderItems) {
        if (topicFolders == null || topicFolderItems == null) {
            return new TopicFolderListDataProvider(new LinkedList<>());
        }

        final List<ResFolder> orderedFolders = new ArrayList<>();

        Observable.from(topicFolders)
                .toSortedList((lhs, rhs) -> lhs.seq - rhs.seq)
                .subscribe(orderedFolders::addAll);

        List<Pair<TopicFolderData,
                List<TopicItemData>>> datas = new LinkedList<>();

        LinkedHashMap<Long, Topic> joinTopics = getJoinEntities();

        long folderIndex = 0;

        Map<Long, List<TopicItemData>> topicItemMap = new HashMap<>();
        Map<Long, TopicFolderData> folderMap = new LinkedHashMap<>();
        Map<Long, Integer> badgeCountMap = new HashMap<>();

        for (ResFolder topicFolder : orderedFolders) {
            if (!topicItemMap.containsKey(topicFolder.id)) {
                topicItemMap.put(topicFolder.id, new ArrayList<>());
            }
            if (!badgeCountMap.containsKey(topicFolder.id)) {
                badgeCountMap.put(topicFolder.id, 0);
            }
            if (!folderMap.containsKey(topicFolder.id)) {
                TopicFolderData topicFolderData = new TopicFolderData(folderIndex, topicFolder.name, topicFolder.id);
                topicFolderData.setSeq(topicFolder.seq);
                folderMap.put(topicFolder.id, topicFolderData);
            }
            folderIndex++;
        }

        Observable.from(topicFolderItems)
                .filter(topicFolderItem -> topicFolderItem.folderId > 0)
                .filter(topicFolderItem -> joinTopics.containsKey(topicFolderItem.roomId))
                .subscribe(topicFolderItem -> {

                    Topic topic = joinTopics.remove(topicFolderItem.roomId);

                    long itemIndex = folderMap.get(topicFolderItem.folderId).generateNewChildId();

                    TopicItemData topicItemData = TopicItemData.newInstance(
                            itemIndex, topic.getCreatorId(), topic.getName(),
                            topic.isStarred(), topic.isJoined(), topic.getEntityId(),
                            topic.getUnreadCount(), topic.getMarkerLinkId(), topic.isPushOn(),
                            topic.isSelected(), topic.getDescription(), topic.isPublic(),
                            topic.getMemberCount());

                    topicItemMap.get(topicFolderItem.folderId).add(topicItemData);

                    int badgeCount = badgeCountMap.get(topicFolderItem.folderId);
                    badgeCountMap.put(topicFolderItem.folderId, badgeCount + topicItemData
                            .getUnreadCount());

                }, Throwable::printStackTrace);

        for (Long folderId : folderMap.keySet()) {

            List<TopicItemData> topicItemDatas = topicItemMap.get(folderId);
            List<TopicItemData> providerTopicItemDatas = new ArrayList<>();

            Collections.sort(topicItemDatas, (lhs, rhs) -> {
                if (lhs.isStarred() && rhs.isStarred()) {
                    return lhs.getName().compareToIgnoreCase(rhs.getName());
                } else if (lhs.isStarred()) {
                    return -1;
                } else if (rhs.isStarred()) {
                    return 1;
                } else {
                    return lhs.getName().compareToIgnoreCase(rhs.getName());
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

    @Background
    public void saveFolderDataInDB(List<ResFolder> topicFolders, List<ResFolderItem> topicFolderItems) {
        TopicFolderRepository repository = TopicFolderRepository.getRepository();
        repository.removeAllFolders();
        repository.removeAllFolderItems();
        repository.insertFolders(topicFolders);
        repository.insertFolderItems(topicFolderItems);
    }

    // 그룹이 없는 Topic 들을 담아낼 더미 그룹 생성
    public TopicFolderData getFakeFolder(long lastFolderIndex) {
        TopicFolderData topicFolderData = new TopicFolderData(lastFolderIndex, "fakeFolder", -1);
        topicFolderData.setIsFakeFolder(true);
        return topicFolderData;
    }

    public void resetBadge(long entityId) {
        EntityManager.getInstance().getEntityById(entityId).alarmCount = 0;
    }

    public boolean isMe(int writer) {
        return EntityManager.getInstance().getMe().getId() == writer;
    }

    public void updateMessageCount(SocketMessageEvent event, List<TopicItemData> joinedTopics) {
        Observable.from(joinedTopics)
                .filter(topicItemData -> {
                    if (!TextUtils.equals(event.getMessageType(), "file_comment")) {
                        if (TextUtils.equals(event.getMessageType(), "topic_join")
                                || TextUtils.equals(event.getMessageType(), "topic_invite")
                                || TextUtils.equals(event.getMessageType(), "topic_leave")
                                || TextUtils.equals(event.getMessageType(), "message_delete")
                                || TextUtils.equals(event.getMessageType(), "file_unshare")) {
                            return false;
                        } else {
                            return topicItemData.getEntityId() == event.getRoom().getId();
                        }
                    } else if (TextUtils.equals(event.getMessageType(), "link_preview_create")) {
                        // 단순 메세지 업데이트인 경우
                        return false;
                    } else {
                        for (SocketMessageEvent.MessageRoom messageRoom : event.getRooms()) {
                            if (topicItemData.getEntityId() == messageRoom.getId()) {
                                return true;
                            }
                        }
                        return false;
                    }
                })
                .doOnNext(topicItemData -> topicItemData.setUnreadCount(topicItemData.getUnreadCount() + 1))
                .doOnNext(topicItemData -> EntityManager.getInstance().getEntityById(topicItemData.getEntityId()).alarmCount++)
                .subscribe();
    }

    public void updateMessageCountForUpdated(SocketMessageEvent event, List<Topic> items) {
        Observable.from(items)
                .filter(topic -> {
                    if (!TextUtils.equals(event.getMessageType(), "file_comment")) {
                        if (TextUtils.equals(event.getMessageType(), "topic_join")
                                || TextUtils.equals(event.getMessageType(), "topic_invite")
                                || TextUtils.equals(event.getMessageType(), "topic_leave")
                                || TextUtils.equals(event.getMessageType(), "message_delete")
                                || TextUtils.equals(event.getMessageType(), "file_unshare")) {
                            return false;
                        } else {
                            return topic.getEntityId() == event.getRoom().getId();
                        }
                    } else if (TextUtils.equals(event.getMessageType(), "link_preview_create")) {
                        // 단순 메세지 업데이트인 경우
                        return false;
                    } else {
                        for (SocketMessageEvent.MessageRoom messageRoom : event.getRooms()) {
                            if (topic.getEntityId() == messageRoom.getId()) {
                                return true;
                            }
                        }
                        return false;
                    }
                })
//                .doOnNext(topic -> topic.setUnreadCount(topic.getUnreadCount() + 1))
                .filter(topic -> event.getLinkId() > 0)
                .doOnNext(topic -> {
                    long entityId = topic.getEntityId();
                    if (topic.isPublic()) {
                        ResLeftSideMenu.Channel channel = EntityManager.getInstance().getEntityById(entityId).getChannel();
                        channel.lastLinkId = event.getLinkId();
                    } else {
                        ResLeftSideMenu.PrivateGroup group = EntityManager.getInstance().getEntityById(entityId).getPrivateGroup();
                        group.lastLinkId = event.getLinkId();
                    }
                })
                .subscribe(topic -> {}, t -> {});
    }

    public boolean isFolderSame(List<ResFolder> folders1, List<ResFolder> folders2) {
        if (folders1.size() != folders2.size()) {
            return false;
        } else {
            Map<Long, ResFolder> folderMap1 = new LinkedHashMap<>();
            Map<Long, ResFolder> folderMap2 = new HashMap<>();
            for (int i = 0; i < folders1.size(); i++) {
                folderMap1.put(folders1.get(i).id, folders1.get(i));
                folderMap2.put(folders2.get(i).id, folders2.get(i));
            }
            for (Long i : folderMap1.keySet()) {
                ResFolder folder1 = folderMap1.get(i);
                ResFolder folder2 = folderMap2.get(i);
                if (!folder1.equals(folder2)) {
                    return false;
                }
            }
            return true;
        }
    }


    public boolean isFolderItemSame(List<ResFolderItem> folderItems1, List<ResFolderItem> folderItems2) {
        if (folderItems1.size() != folderItems2.size()) {
            return false;
        } else {
            Map<Long, ResFolderItem> folderItemMap1 = new LinkedHashMap<>();
            Map<Long, ResFolderItem> folderItemMap2 = new HashMap<>();
            for (int i = 0; i < folderItems1.size(); i++) {
                folderItemMap1.put(folderItems1.get(i).roomId, folderItems1.get(i));
                folderItemMap2.put(folderItems2.get(i).roomId, folderItems2.get(i));
            }
            for (Long i : folderItemMap1.keySet()) {
                ResFolderItem folderItem1 = folderItemMap1.get(i);
                ResFolderItem folderItem2 = folderItemMap2.get(i);
                if (!folderItem1.equals(folderItem2)) {
                    return false;
                }
            }
            return true;
        }
    }

    public Observable<List<Topic>> getUpdatedTopicList() {
        EntityManager entityManager = EntityManager.getInstance();
        return Observable.from(entityManager.getJoinedChannels())
                .map(entity -> {
                    ResLeftSideMenu.Channel channel = entity.getChannel();
                    return new Topic.Builder()
                            .name(entity.getName())
                            .isStarred(entity.isStarred)
                            .isJoined(true)
                            .entityId(entity.getId())
                            .memberCount(entity.getMemberCount())
                            .unreadCount(entity.alarmCount)
                            .isPublic(true)
                            .description(entity.getDescription())
                            .creatorId(channel.ch_creatorId)
                            .markerLinkId(entity.lastLinkId)
                            .lastLinkId(channel.lastLinkId)
                            .isPushOn(entity.isTopicPushOn)
                            .build();
                })
                .mergeWith(Observable.from(entityManager.getGroups())
                        .map(entity -> {
                            ResLeftSideMenu.PrivateGroup privateGroup = entity.getPrivateGroup();
                            return new Topic.Builder()
                                    .name(entity.getName())
                                    .isStarred(entity.isStarred)
                                    .isJoined(true)
                                    .entityId(entity.getId())
                                    .memberCount(entity.getMemberCount())
                                    .unreadCount(entity.alarmCount)
                                    .isPublic(false)
                                    .description(entity.getDescription())
                                    .creatorId(privateGroup.pg_creatorId)
                                    .markerLinkId(entity.lastLinkId)
                                    .lastLinkId(privateGroup.lastLinkId)
                                    .isPushOn(entity.isTopicPushOn)
                                    .build();
                        }))
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

    public int getUnreadCount() {
        final int[] unreadCount = {0};
        EntityManager entityManager = EntityManager.getInstance();
        Observable.merge(Observable.from(entityManager.getJoinedChannels()), Observable.from(entityManager.getGroups()))
                .subscribe(entity -> unreadCount[0] += entity.alarmCount, t -> {});

        return unreadCount[0];
    }

    public long findFolderId(long entityId) {
        return TopicFolderRepository.getRepository().getFolderOfTopic(entityId).folderId;
    }
}