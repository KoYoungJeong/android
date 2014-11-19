package com.tosslab.jandi.app.lists.entities;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class EntityManager {
    private final Logger log = Logger.getLogger(EntityManager.class);

    private ResLeftSideMenu.Team mMyTeam;
    private ResLeftSideMenu.User mMe;   // with MessageMarker
    private HashMap<Integer, FormattedEntity> mJoinedTopics;
    private HashMap<Integer, FormattedEntity> mUnjoinedTopics;
    private HashMap<Integer, FormattedEntity> mUsers;
    private HashMap<Integer, FormattedEntity> mGroups;

    private HashMap<Integer, FormattedEntity> mStarredJoinedTopics;
    private HashMap<Integer, FormattedEntity> mStarredUsers;
    private HashMap<Integer, FormattedEntity> mStarredGroups;

    private HashMap<Integer, ResLeftSideMenu.MessageMarker> mMarkers;

    // Collection 의 Sort 는 연산 시간이 오래 걸리기 때문에 한번만 하기 위해 저장하자.
    private List<FormattedEntity> mSortedJoinedTopics = null;
    private List<FormattedEntity> mSortedUnjoinedTopics = null;
    private List<FormattedEntity> mSortedUsers = null;
    private List<FormattedEntity> mSortedUsersWithoutMe = null;
    private List<FormattedEntity> mSortedGroups = null;

    private int mTotalBadgeCount;

    public EntityManager(ResLeftSideMenu resLeftSideMenu) {
        mJoinedTopics = new HashMap<Integer, FormattedEntity>();
        mUnjoinedTopics = new HashMap<Integer, FormattedEntity>();
        mGroups = new HashMap<Integer, FormattedEntity>();
        mUsers = new HashMap<Integer, FormattedEntity>();

        mStarredJoinedTopics = new HashMap<Integer, FormattedEntity>();
        mStarredGroups = new HashMap<Integer, FormattedEntity>();
        mStarredUsers = new HashMap<Integer, FormattedEntity>();

        mMarkers = new HashMap<Integer, ResLeftSideMenu.MessageMarker>();

        this.mMyTeam = resLeftSideMenu.team;
        this.mMe = resLeftSideMenu.user;

        for (ResLeftSideMenu.MessageMarker marker : mMe.u_messageMarkers) {
            mMarkers.put(marker.entityId, marker);
        }
        arrangeEntities(resLeftSideMenu);
        mTotalBadgeCount = retrieveTotalBadgeCount();
    }

    /************************************************************
     * for Badge Count
     ************************************************************/
    private int retrieveTotalBadgeCount() {
        int totalBadgeCount = 0;

        totalBadgeCount += retrieveBadgeCount(mStarredJoinedTopics);
        totalBadgeCount += retrieveBadgeCount(mJoinedTopics);
        totalBadgeCount += retrieveBadgeCount(mStarredGroups);
        totalBadgeCount += retrieveBadgeCount(mGroups);
        totalBadgeCount += retrieveBadgeCount(mStarredUsers);
        totalBadgeCount += retrieveBadgeCount(mUsers);

        return totalBadgeCount;
    }

    private int retrieveBadgeCount(HashMap<Integer, FormattedEntity> hashMap) {
        int badgeCount = 0;
        for (FormattedEntity formattedEntity : hashMap.values()) {
            badgeCount += formattedEntity.alarmCount;
        }
        return badgeCount;
    }

    public int getTotalBadgeCount() {
        return mTotalBadgeCount;
    }

    /**
     * 현재 entity에 해당하는 Marker가 존재하는지 확인하여 있으면 정보를 추가한다
     * @param entity
     * @return
     */
    private FormattedEntity patchMarkerToFormattedEntity(FormattedEntity entity) {
        if (mMarkers.containsKey(entity.getId())) {
            ResLeftSideMenu.MessageMarker marker = mMarkers.get(entity.getId());
            entity.lastLinkId = marker.lastLinkId;
            entity.alarmCount = marker.alarmCount;
        }
        return entity;
    }

    private void arrangeEntities(ResLeftSideMenu resLeftSideMenu) {
        // HashTable 로 빼야하나? 즐겨찾기처럼 길이가 작을 경우 어떤게 더 유리한지 모르겠넹~
        List<Integer> starredEntities = (resLeftSideMenu.user.u_starredEntities != null)
                ? resLeftSideMenu.user.u_starredEntities
                : new ArrayList<Integer>();

        // Unjoined topic 혹은 User 리스트 정리
        for (ResLeftSideMenu.Entity entity : resLeftSideMenu.entities) {
            if (entity instanceof ResLeftSideMenu.Channel) {
                FormattedEntity unjoinedTopic
                        = new FormattedEntity((ResLeftSideMenu.Channel) entity, FormattedEntity.UNJOINED);
                unjoinedTopic = patchMarkerToFormattedEntity(unjoinedTopic);
                mUnjoinedTopics.put(entity.id, unjoinedTopic);

            } else if (entity instanceof ResLeftSideMenu.User) {
                FormattedEntity user = new FormattedEntity((ResLeftSideMenu.User) entity);
                user = patchMarkerToFormattedEntity(user);
                if (starredEntities.contains(entity.id)) {
                    user.isStarred = true;
                    mStarredUsers.put(entity.id, user);
                } else {
                    mUsers.put(entity.id, user);
                }
            } else {
                // DO NOTHING
            }
        }

        // Joined Channel 혹은 PrivateGroup 리스트 정리
        for (ResLeftSideMenu.Entity entity : resLeftSideMenu.joinEntities) {
            if (entity instanceof ResLeftSideMenu.Channel) {
                // 만일 unjoined topic 에 해당 topic 이 있다면 뺀다.
                mUnjoinedTopics.remove(entity.id);
                FormattedEntity joinedTopic
                        = new FormattedEntity((ResLeftSideMenu.Channel) entity, FormattedEntity.JOINED);
                joinedTopic = patchMarkerToFormattedEntity(joinedTopic);
                if (starredEntities.contains(entity.id)) {
                    joinedTopic.isStarred = true;
                    mStarredJoinedTopics.put(entity.id, joinedTopic);
                } else {
                    mJoinedTopics.put(entity.id, joinedTopic);
                }

            } else if (entity instanceof ResLeftSideMenu.PrivateGroup) {
                ResLeftSideMenu.PrivateGroup privateGroup = (ResLeftSideMenu.PrivateGroup) entity;
                FormattedEntity group = new FormattedEntity(privateGroup);
                group = patchMarkerToFormattedEntity(group);
                if (starredEntities.contains(entity.id)) {
                    group.isStarred = true;
                    mStarredGroups.put(entity.id, group);
                } else {
                    mGroups.put(entity.id, group);
                }
            } else {
                // DO NOTHING
            }
        }

        // Sort 도 다시해야 하기 때문에 해당 List 들을 초기화
        zeroizeSortedEntityList();
    }

    private void zeroizeSortedEntityList() {
        mSortedJoinedTopics = null;
        mSortedUnjoinedTopics = null;
        mSortedUsers = null;
        mSortedUsersWithoutMe = null;
        mSortedGroups = null;
    }

    /************************************************************
     * Getter
     ************************************************************/
    public List<FormattedEntity> getJoinedChannels() {
        ArrayList<FormattedEntity> ret = new ArrayList<FormattedEntity>();
        if (mSortedJoinedTopics == null) {
            mSortedJoinedTopics = sortFormattedEntityList(mJoinedTopics.values());
        }
        ret.addAll(mStarredJoinedTopics.values());
        ret.addAll(mSortedJoinedTopics);
        return ret;
    }

    public List<FormattedEntity> getUnjoinedChannels() {
        if (mSortedUnjoinedTopics == null) {
            mSortedUnjoinedTopics = sortFormattedEntityList(mUnjoinedTopics.values());
        }
        return mSortedUnjoinedTopics;
    }

    public List<FormattedEntity> getGroups() {
        ArrayList<FormattedEntity> ret = new ArrayList<FormattedEntity>();
        if (mSortedGroups == null) {
            mSortedGroups = sortFormattedEntityList(mGroups.values());
        }
        ret.addAll(mStarredGroups.values());
        ret.addAll(mSortedGroups);
        return ret;
    }

    public List<FormattedEntity> getFormattedUsers() {
        ArrayList<FormattedEntity> ret = new ArrayList<FormattedEntity>();
        if (mSortedUsers == null) {
            mSortedUsers = sortFormattedEntityList(mUsers.values());
        }
        ret.addAll(mStarredUsers.values());
        ret.addAll(mSortedUsers);
        return ret;
    }

    public List<FormattedEntity> getFormattedUsersWithoutMe() {
        ArrayList<FormattedEntity> ret = new ArrayList<FormattedEntity>();
        if (mSortedUsersWithoutMe == null) {
            HashMap<Integer, FormattedEntity> usersWithoutMe
                    = (HashMap<Integer, FormattedEntity>)mUsers.clone();
            usersWithoutMe.remove(mMe.id);
            mSortedUsersWithoutMe = sortFormattedEntityList(usersWithoutMe.values());
        }
        ret.addAll(mStarredUsers.values());
        ret.addAll(mSortedUsersWithoutMe);
        return ret;
    }

    public List<FormattedEntity> getCategorizableEntities() {
        List<FormattedEntity> formattedEntities = new ArrayList<FormattedEntity>();
        formattedEntities.add(new FormattedEntity(FormattedEntity.TYPE_EVERYWHERE));
        formattedEntities.addAll(retrieveAccessableEntities());
        return formattedEntities;
    }

    public FormattedEntity getMe() {
        return new FormattedEntity(mMe);
    }

    public String getDistictId() {
        return  mMe.id+ "@" + mMyTeam.id;
    }

    public String getTeamName() {
        return mMyTeam.name;
    }

    public int getTeamId() {
        return mMyTeam.id;
    }

    /**
     * 인자로 주어진 ID에 해당하는 CDP를 추출한다.
     * @param givenEntityIds
     * @return
     */
    public List<FormattedEntity> retrieveGivenEntities(List<Integer> givenEntityIds) {
        return retrieveByGivenEntities(givenEntityIds, true);
    }

    /**
     * 인자로 주어진 ID 를 제외한 공유 대상 CDP를 추출한다.
     * @param givenEntityIds
     * @return
     */
    public List<FormattedEntity> retrieveExclusivedEntities(List<Integer> givenEntityIds) {
        return retrieveByGivenEntities(givenEntityIds, false);
    }

    private List<FormattedEntity> retrieveByGivenEntities(List<Integer> givenEntityIds, boolean includable) {
        List<FormattedEntity> accessableEntities = retrieveAccessableEntities();
        ArrayList<FormattedEntity> retCdpItems = new ArrayList<FormattedEntity>();

        for (FormattedEntity accessableEntity : accessableEntities) {
            if (accessableEntity.hasGivenIds(givenEntityIds) == includable) {
                retCdpItems.add(accessableEntity);
            }
        }
        return retCdpItems;
    }

    public List<FormattedEntity> retrieveAccessableEntities() {
        List<FormattedEntity> entities = new ArrayList<FormattedEntity>();
        entities.addAll(getJoinedChannels());
        entities.addAll(getGroups());
        entities.addAll(getFormattedUsers());
        return entities;
    }

    public FormattedEntity getEntityById(int entityId) {
        FormattedEntity topic = searchTopicById(entityId);
        if (topic != null) {
            return topic;
        }
        FormattedEntity user = searchUserById(entityId);
        if (user != null) {
            return user;
        }
        FormattedEntity group = searchGroupById(entityId);
        if (group != null) {
            return group;
        }

        return null;
    }

    public String getEntityNameById(int cdpId) {
        FormattedEntity entity = getEntityById(cdpId);
        return (entity != null) ? entity.getName() : "";
    }

    public List<FormattedEntity> getUnjoinedMembersOfEntity(int entityId, int entityType) {
        FormattedEntity entity;
        if (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) {
            entity = searchTopicById(entityId);
        } else if (entityType == JandiConstants.TYPE_PRIVATE_TOPIC) {
            entity = searchGroupById(entityId);
        } else {
            return null;
        }
        return extractExclusivedUser(entity.getMembers());
    }

    private List<FormattedEntity> extractExclusivedUser(List<Integer> joinedMembers) {
        ArrayList<FormattedEntity> ret = new ArrayList<FormattedEntity>();

        HashMap<Integer, FormattedEntity> starredUsers
                = (HashMap<Integer, FormattedEntity>)mStarredUsers.clone();
        HashMap<Integer, FormattedEntity> users
                = (HashMap<Integer, FormattedEntity>)mUsers.clone();

        for (int id : joinedMembers) {
            starredUsers.remove(id);
            users.remove(id);
        }
        ret.addAll(starredUsers.values());
        ret.addAll(users.values());
        return ret;
    }

    /************************************************************
     * 각 채팅 종류별로 새로운 메시지가 있는지 확인
     * 뱃지 카운트의 존재 여부를 검사하여...
     ************************************************************/
    public boolean hasNewTopicMessage() {
        return hasNewMessage(getJoinedChannels());
    }

    public boolean hasNewChatMessage() {
        return hasNewMessage(getGroups())
                || hasNewMessage(getFormattedUsersWithoutMe());
    }

    private boolean hasNewMessage(List<FormattedEntity> formattedEntities) {
        for (FormattedEntity entity : formattedEntities) {
            if (entity.alarmCount > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isMyEntity(int entityId) {
        FormattedEntity searchedEntity = searchTopicById(entityId);
        if (searchedEntity != null) {
            return searchedEntity.isMine(mMe.id);
        }

        return false;
    }

    public boolean isMe(int userId) {
        return (getMe().getId() == userId);
    }

    private FormattedEntity searchTopicById(int topicId) {
        if (mStarredJoinedTopics.containsKey(topicId)) {
            return mStarredJoinedTopics.get(topicId);
        }
        if (mJoinedTopics.containsKey(topicId)) {
            return mJoinedTopics.get(topicId);
        }
        if (mUnjoinedTopics.containsKey(topicId)) {
            return mUnjoinedTopics.get(topicId);
        }
        return null;
    }

    private FormattedEntity searchGroupById(int groupId) {
        if (mStarredGroups.containsKey(groupId)) {
            return mStarredGroups.get(groupId);
        }
        if (mGroups.containsKey(groupId)) {
            return mGroups.get(groupId);
        }
        return null;
    }

    private FormattedEntity searchUserById(int userId) {
        if (mStarredUsers.containsKey(userId)) {
            return mStarredUsers.get(userId);
        }
        if (mUsers.containsKey(userId)) {
            return mUsers.get(userId);
        }
        return null;
    }

    /**
     * 오름 차순 정렬
     */
    private List<FormattedEntity> sortFormattedEntityList(Collection<FormattedEntity> naiveEntities) {
        List<FormattedEntity> sortedEntities = new ArrayList<FormattedEntity>(naiveEntities);
        Collections.sort(sortedEntities, new NameAscCompare());

        return sortedEntities;
    }

    static class NameAscCompare implements Comparator<FormattedEntity> {

        @Override
        public int compare(FormattedEntity formattedEntity, FormattedEntity formattedEntity2) {
            return formattedEntity.getName().compareToIgnoreCase(formattedEntity2.getName());
        }
    }
}
