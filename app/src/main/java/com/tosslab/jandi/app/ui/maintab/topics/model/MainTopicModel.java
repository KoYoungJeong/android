package com.tosslab.jandi.app.ui.maintab.topics.model;

import android.support.v4.util.Pair;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.lists.libs.advancerecyclerview.provider.AbstractExpandableDataProvider;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.network.models.ResFolderItem;
import com.tosslab.jandi.app.ui.maintab.topics.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.topics.domain.TopicFolderData;
import com.tosslab.jandi.app.ui.maintab.topics.domain.TopicFolderListDataProvider;
import com.tosslab.jandi.app.ui.maintab.topics.domain.TopicItemData;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import rx.Observable;

/**
 * Created by tee on 15. 8. 26..
 */

@EBean
public class MainTopicModel {

    @Bean
    EntityClientManager entityClientManager;

    // 폴더 정보 가져오기
    public List<ResFolder> getTopicFolders() {
        return RequestApiManager.getInstance()
                .getFoldersByTeamApi(entityClientManager.getSelectedTeamId());
    }

    // 폴더 속 토픽 아이디 가져오기
    public List<ResFolderItem> getTopicFolderItems() {
        return RequestApiManager.getInstance()
                .getFolderItemsByTeamApi(entityClientManager.getSelectedTeamId());
    }

    // Join된 Topic에 관한 정보를 가져오기
    public LinkedHashMap<Integer, Topic> getJoinEntities() {

        EntityManager entityManager = EntityManager.getInstance();

        List<FormattedEntity> joinedChannels = entityManager.getJoinedChannels();
        List<FormattedEntity> groups = entityManager.getGroups();
        LinkedHashMap topicHashMap = new LinkedHashMap<Integer, Topic>();

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
    public TopicFolderListDataProvider getDataProvider() {

        List<Pair<AbstractExpandableDataProvider.GroupData,
                List<AbstractExpandableDataProvider.ChildData>>> datas = new LinkedList<>();

        List<ResFolder> topicFolders = getTopicFolders();
        List<ResFolderItem> topicFolderItems = getTopicFolderItems();
        LinkedHashMap<Integer, Topic> joinTopics = getJoinEntities();

        long folderIndex = 0;

        // 각 폴더와 종속된 토픽 데이터 셋팅
        for (ResFolder folder : topicFolders) {

            TopicFolderData topicFolderData = new TopicFolderData(folderIndex, folder.name, folder.id, -1);
            long itemBadgeCount = 0;
            int itemCount = 0;
            List<AbstractExpandableDataProvider.ChildData> topicItemDatas = new ArrayList<>();

            for (ResFolderItem folderItem : topicFolderItems) {
                if (folderItem.folderId != -1 && folderItem.folderId == folder.id) {
                    itemBadgeCount = 0;
                    Topic topic = joinTopics.get(folderItem.roomId);
                    if (topic != null) {
                        joinTopics.remove(folderItem.roomId);
                        long itemIndex = topicFolderData.generateNewChildId();
                        TopicItemData topicItemData = TopicItemData.newInstance(
                                itemIndex, -1, topic.getCreatorId(), topic.getName(),
                                topic.isStarred(), topic.isJoined(), topic.getEntityId(),
                                topic.getUnreadCount(), topic.getMarkerLinkId(), topic.isPushOn(),
                                topic.isSelected(), topic.getDescription(), topic.isPublic(),
                                topic.getMemberCount());
                        itemCount++;
                        itemBadgeCount += Integer.valueOf(topic.getUnreadCount());
                        topicItemDatas.add(topicItemData);
                    }
                }
            }


            topicFolderData.setItemCount(itemCount);
            topicFolderData.setChildBadgeCnt(itemBadgeCount);

            datas.add(new Pair(topicFolderData, topicItemDatas));

            folderIndex++;

        }

        // 폴더가 없는 토픽 데이터 셋팅
        TopicFolderData fakeFolder = getFakeFolder(folderIndex);
        Iterator joinTopicKeySets = joinTopics.keySet().iterator();

        List<AbstractExpandableDataProvider.ChildData> noFolderTopicItemDatas = new ArrayList<>();

        while (joinTopicKeySets.hasNext()) {
            long itemIndex = fakeFolder.generateNewChildId();
            Topic topic = joinTopics.get((Integer) joinTopicKeySets.next());
            if (topic != null) {
                TopicItemData topicItemData = TopicItemData.newInstance(
                        itemIndex, -1, topic.getCreatorId(), topic.getName(),
                        topic.isStarred(), topic.isJoined(), topic.getEntityId(),
                        topic.getUnreadCount(), topic.getMarkerLinkId(), topic.isPushOn(),
                        topic.isSelected(), topic.getDescription(), topic.isPublic(), topic.getMemberCount());
                noFolderTopicItemDatas.add(topicItemData);
            }
        }

        // Topic join button을 위한 더미 인스턴스 추가
        noFolderTopicItemDatas.add(TopicItemData.getDummyInstance());

        datas.add(new Pair(fakeFolder, noFolderTopicItemDatas));

        return new TopicFolderListDataProvider(datas);
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

    public boolean hasAlarmCount(Observable<Topic> joinEntities) {

        Topic defaultValue = new Topic.Builder().build();
        Topic first = joinEntities.filter(topic -> topic.getUnreadCount() > 0)
                .firstOrDefault(defaultValue)
                .toBlocking()
                .first();

        return first != defaultValue;

    }

}