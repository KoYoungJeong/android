package com.tosslab.jandi.app.ui.maintab.topic.model;

import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.lists.libs.advancerecyclerview.provider.AbstractExpandableDataProvider;
import com.tosslab.jandi.app.local.orm.repositories.TopicFolderRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.network.models.ResFolderItem;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicFolderData;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicFolderListDataProvider;
import com.tosslab.jandi.app.ui.maintab.topic.domain.TopicItemData;

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

/**
 * Created by tee on 15. 8. 26..
 */

@EBean
public class MainTopicModel {

    @Bean
    EntityClientManager entityClientManager;

    // 폴더 정보 가져오기
    public List<ResFolder> getTopicFolders() throws RetrofitError {
        return RequestApiManager.getInstance()
                .getFoldersByTeamApi(entityClientManager.getSelectedTeamId());
    }

    // 폴더 속 토픽 아이디 가져오기
    public List<ResFolderItem> getTopicFolderItems() throws RetrofitError {
        return RequestApiManager.getInstance()
                .getFolderItemsByTeamApi(entityClientManager.getSelectedTeamId());
    }

    // Join된 Topic에 관한 정보를 가져오기
    public LinkedHashMap<Integer, Topic> getJoinEntities() {

        EntityManager entityManager = EntityManager.getInstance();

        List<FormattedEntity> joinedChannels = entityManager.getJoinedChannels();
        List<FormattedEntity> groups = entityManager.getGroups();
        LinkedHashMap<Integer, Topic> topicHashMap = new LinkedHashMap<>();

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

        List<Pair<AbstractExpandableDataProvider.GroupData,
                List<AbstractExpandableDataProvider.ChildData>>> datas = new LinkedList<>();

        LinkedHashMap<Integer, Topic> joinTopics = getJoinEntities();

        long folderIndex = 0;

        Map<Integer, List<TopicItemData>> topicItemMap = new HashMap<>();
        Map<Integer, TopicFolderData> folderMap = new LinkedHashMap<>();
        Map<Integer, Integer> badgeCountMap = new HashMap<>();

        for (ResFolder topicFolder : topicFolders) {
            if (!topicItemMap.containsKey(topicFolder.id)) {
                topicItemMap.put(new Integer(topicFolder.id), new ArrayList<>());
            }

            if (!badgeCountMap.containsKey(topicFolder.id)) {
                badgeCountMap.put(new Integer(topicFolder.id), 0);
            }

            if (!folderMap.containsKey(new Integer(topicFolder.id))) {
                TopicFolderData topicFolderData = new TopicFolderData(folderIndex, topicFolder.name, topicFolder.id, -1);
                topicFolderData.setSeq(topicFolder.seq);
                folderMap.put(new Integer(topicFolder.id), topicFolderData);
            }
            folderIndex++;
        }

        Observable.from(topicFolderItems)
                .filter(topicFolderItem -> topicFolderItem.folderId > 0)
                .subscribe(topicFolderItem -> {
                    Topic topic = joinTopics.remove(new Integer(topicFolderItem.roomId));

                    long itemIndex = folderMap.get(new Integer(topicFolderItem.folderId)).generateNewChildId();

                    TopicItemData topicItemData = TopicItemData.newInstance(
                            itemIndex, -1, topic.getCreatorId(), topic.getName(),
                            topic.isStarred(), topic.isJoined(), topic.getEntityId(),
                            topic.getUnreadCount(), topic.getMarkerLinkId(), topic.isPushOn(),
                            topic.isSelected(), topic.getDescription(), topic.isPublic(),
                            topic.getMemberCount());

                    topicItemMap.get(new Integer(topicFolderItem.folderId)).add(topicItemData);

                    int badgeCount = badgeCountMap.get(new Integer(topicFolderItem.folderId));
                    badgeCountMap.put(new Integer(topicFolderItem.folderId), badgeCount + topicItemData
                            .getUnreadCount());
                }, Throwable::printStackTrace);

        for (Integer folderId : folderMap.keySet()) {

            List<TopicItemData> topicItemDatas = topicItemMap.get(folderId);
            List<AbstractExpandableDataProvider.ChildData> providerTopicItemDatas = new ArrayList<>();

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
//        Iterator<Integer> joinTopicKeySets = joinTopics.keySet().iterator();

        List<AbstractExpandableDataProvider.ChildData> noFolderTopicItemDatas = new ArrayList<>();

        Observable.from(joinTopics.keySet())
                .map(topicId -> {
                    long itemIndex = fakeFolder.generateNewChildId();
                    Topic topic = joinTopics.get(topicId);
                    return TopicItemData.newInstance(
                            itemIndex, -1, topic.getCreatorId(), topic.getName(),
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
        TopicFolderData topicFolderData = new TopicFolderData(lastFolderIndex, "fakeFolder", -1, -1);
        topicFolderData.setIsFakeFolder(true);
        return topicFolderData;
    }

    public void resetBadge(int entityId) {
        EntityManager.getInstance().getEntityById(entityId).alarmCount = 0;
    }

    public boolean isMe(int writer) {
        return EntityManager.getInstance().getMe().getId() == writer;
    }

    public void updateMessageCount(SocketMessageEvent event, List<TopicItemData> joinedTopics) {
        TopicItemData dummyInstance = TopicItemData.getDummyInstance();
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
                .firstOrDefault(dummyInstance)
                .subscribe();

    }
}