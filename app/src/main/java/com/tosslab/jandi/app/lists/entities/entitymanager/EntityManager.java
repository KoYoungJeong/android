package com.tosslab.jandi.app.lists.entities.entitymanager;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.StringCompareUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class EntityManager {

    public static FormattedEntity UNKNOWN_USER_ENTITY;
    private static EntityManager entityManager;

    static {
        ResLeftSideMenu.User unknownUser = new ResLeftSideMenu.User();
        unknownUser.id = -1;
        unknownUser.name = "";

        unknownUser.u_email = "";
        unknownUser.u_authority = "";
        unknownUser.accountId = "";
        unknownUser.status = "disabled";
        unknownUser.createdAt = new Date();
        unknownUser.u_statusMessage = "";

        unknownUser.u_extraData = new ResLeftSideMenu.ExtraData();
        unknownUser.u_extraData.department = "";
        unknownUser.u_extraData.phoneNumber = "";
        unknownUser.u_extraData.position = "";

        unknownUser.u_photoUrl = "";
        unknownUser.u_photoThumbnailUrl = new ResMessages.ThumbnailUrls();
        unknownUser.u_photoThumbnailUrl.largeThumbnailUrl = "";
        unknownUser.u_photoThumbnailUrl.mediumThumbnailUrl = "";
        unknownUser.u_photoThumbnailUrl.smallThumbnailUrl = "";

        unknownUser.u_messageMarkers = new ArrayList<>();
        unknownUser.u_starredEntities = new ArrayList<>();

        UNKNOWN_USER_ENTITY = new FormattedEntity(unknownUser);
    }

    private ResLeftSideMenu.Team mMyTeam;
    private ResLeftSideMenu.User mMe;   // with MessageMarker

    private Map<Long, FormattedEntity> mJoinedTopics = new HashMap<>();
    private Map<Long, FormattedEntity> mUnjoinedTopics = new HashMap<>();
    private Map<Long, FormattedEntity> mUsers = new HashMap<>();
    private Map<Long, FormattedEntity> mJoinedUsers = new HashMap<>();
    private Map<Long, FormattedEntity> mGroups = new HashMap<>();

    private Map<Long, FormattedEntity> mStarredJoinedTopics = new HashMap<>();
    private Map<Long, FormattedEntity> mStarredUsers = new HashMap<>();
    private Map<Long, FormattedEntity> mStarredGroups = new HashMap<>();

    private Map<Long, ResLeftSideMenu.MessageMarker> mMarkers = new HashMap<>();

    // Collection 의 Sort 는 연산 시간이 오래 걸리기 때문에 한번만 하기 위해 저장하자.
    private List<FormattedEntity> mSortedJoinedTopics = null;
    private List<FormattedEntity> mSortedUnjoinedTopics = null;
    private List<FormattedEntity> mSortedUsers = null;
    private List<FormattedEntity> mSortedUsersWithoutMe = null;
    private List<FormattedEntity> mSortedGroups = null;
    private Map<Long, BotEntity> bots = new HashMap<>();

    protected EntityManager() {
        ResLeftSideMenu resLeftSideMenu = LeftSideMenuRepository.getRepository().getCurrentLeftSideMenu();
        if (resLeftSideMenu != null) {
            init(resLeftSideMenu);
        }
    }

    public static synchronized EntityManager getInstance() {
        if (entityManager == null) {
            entityManager = new EntityManagerLockProxy();
        }
        return entityManager;
    }

    protected void init(ResLeftSideMenu resLeftSideMenu) {
        mJoinedTopics.clear();
        mUnjoinedTopics.clear();
        mGroups.clear();
        mUsers.clear();
        mJoinedUsers.clear();

        mStarredJoinedTopics.clear();
        mStarredGroups.clear();
        mStarredUsers.clear();

        mMarkers.clear();

        this.mMyTeam = resLeftSideMenu.team;
        this.mMe = resLeftSideMenu.user;

        for (ResLeftSideMenu.MessageMarker marker : mMe.u_messageMarkers) {
            mMarkers.put(marker.entityId, marker);
        }
        bots.clear();

        arrangeEntities(resLeftSideMenu);

    }

    public void refreshEntity() {

        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();

        if (selectedTeamInfo != null) {
            ResLeftSideMenu resLeftSideMenu = LeftSideMenuRepository.getRepository().getCurrentLeftSideMenu();
            init(resLeftSideMenu);
        }
    }

    // for only test! Don't use this method in nonTest Code
    @Deprecated
    public void refreshEntity(ResLeftSideMenu resLeftSideMenu) {
        init(resLeftSideMenu);
    }

    /**
     * 현재 entity에 해당하는 Marker가 존재하는지 확인하여 있으면 정보를 추가한다
     *
     * @param entity
     * @return
     */
    private FormattedEntity patchMarkerToFormattedEntity(FormattedEntity entity) {
        if (mMarkers.containsKey(entity.getId())) {
            ResLeftSideMenu.MessageMarker marker = mMarkers.get(entity.getId());
            entity.lastLinkId = marker.lastLinkId;
            entity.alarmCount = marker.alarmCount;
            entity.announcementOpened = marker.announcementOpened;
            entity.isTopicPushOn = marker.subscribe;
        }
        return entity;
    }

    private void arrangeEntities(ResLeftSideMenu resLeftSideMenu) {
        // HashTable 로 빼야하나? 즐겨찾기처럼 길이가 작을 경우 어떤게 더 유리한지 모르겠넹~
        LogUtil.d("EntityManger.arrangeEntities");
        Collection<Long> starredEntities =
                (resLeftSideMenu.user.u_starredEntities != null)
                        ? resLeftSideMenu.user.u_starredEntities
                        : new ArrayList<>();

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
            } else if (entity instanceof ResLeftSideMenu.User) {

                ResLeftSideMenu.User user = (ResLeftSideMenu.User) entity;

                mJoinedUsers.put(user.id, new FormattedEntity(user));

            } else {
                // DO NOTHING
            }
        }

        if (resLeftSideMenu.bots != null) {
            Observable.from(resLeftSideMenu.bots)
                    .map(BotEntity::new)
                    .doOnNext(this::patchMarkerToFormattedEntity)
                    .doOnNext(botEntity -> {
                        botEntity.isStarred = starredEntities.contains(botEntity.getId());
                    })
                    .collect(() -> bots, (botEntities, bot) -> botEntities.put(bot.getId(), bot))
                    .subscribe();
        }

        // Sort 도 다시해야 하기 때문에 해당 List 들을 초기화
        zeroizeSortedEntityList();
        // Parse 에 등록된 채널들과 동기화
    }

    private void zeroizeSortedEntityList() {
        mSortedJoinedTopics = null;
        mSortedUnjoinedTopics = null;
        mSortedUsers = null;
        mSortedUsersWithoutMe = null;
        mSortedGroups = null;
    }

    /**
     * *********************************************************
     * Getter
     * **********************************************************
     */
    public List<FormattedEntity> getJoinedChannels() {
        ArrayList<FormattedEntity> ret = new ArrayList<>();
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
        ArrayList<FormattedEntity> ret = new ArrayList<>();
        if (mSortedGroups == null) {
            mSortedGroups = sortFormattedEntityList(mGroups.values());
        }
        ret.addAll(mStarredGroups.values());
        ret.addAll(mSortedGroups);
        return ret;
    }

    public List<FormattedEntity> getFormattedUsers() {
        ArrayList<FormattedEntity> ret = new ArrayList<>();
        if (mSortedUsers == null) {
            mSortedUsers = sortFormattedEntityList(mUsers.values());
        }
        ret.addAll(mStarredUsers.values());
        ret.addAll(mSortedUsers);
        return ret;
    }

    public List<FormattedEntity> getFormattedUsersWithoutMe() {
        ArrayList<FormattedEntity> ret = new ArrayList<>();

        if (mStarredUsers != null) {
            Observable.from(mStarredUsers.values())
                    .filter(formattedEntity -> formattedEntity.getId() != mMe.id)
                    .collect(() -> ret, (o, formattedEntity1) -> o.add(formattedEntity1))
                    .subscribe();
        }

        if (mSortedUsersWithoutMe == null && mUsers != null) {

            List<FormattedEntity> tempEntities = new ArrayList<>();

            Observable.from(mUsers.values())
                    .filter(formattedEntity -> formattedEntity.getId() != mMe.id)
                    .collect(() -> tempEntities, (o, formattedEntity1) -> o.add(formattedEntity1))
                    .subscribe();

            mSortedUsersWithoutMe = sortFormattedEntityList(tempEntities);
        }

        ret.addAll(mSortedUsersWithoutMe);

        return ret;
    }

    public List<FormattedEntity> getCategorizableEntities() {
        List<FormattedEntity> formattedEntities = new ArrayList<>();
        formattedEntities.add(new FormattedEntity(FormattedEntity.TYPE_EVERYWHERE));
        formattedEntities.addAll(retrieveAccessableEntities());
        return formattedEntities;
    }

    public FormattedEntity getMe() {
        return new FormattedEntity(mMe);
    }

    public String getDistictId() {
        // FIXME Why null???
        if (mMe != null && mMyTeam != null) {
            return mMe.id + "-" + mMyTeam.id;
        } else {
            return "unknown";
        }
    }

    public String getTeamName() {
        return mMyTeam.name;
    }

    public long getDefaultTopicId() {
        return mMyTeam.t_defaultChannelId;
    }

    public long getTeamId() {
        return mMyTeam.id;
    }

    /**
     * 인자로 주어진 ID에 해당하는 CDP를 추출한다.
     *
     * @param givenEntityIds
     * @return
     */
    public List<FormattedEntity> retrieveGivenEntities(List<Long> givenEntityIds) {
        return retrieveByGivenEntities(givenEntityIds, true);
    }

    /**
     * 인자로 주어진 ID 를 제외한 공유 대상 CDP를 추출한다.
     *
     * @param givenEntityIds
     * @return
     */
    public List<FormattedEntity> retrieveExclusivedEntities(List<Long> givenEntityIds) {
        return retrieveByGivenEntities(givenEntityIds, false);
    }

    private List<FormattedEntity> retrieveByGivenEntities(List<Long> givenEntityIds, boolean
            includable) {
        List<FormattedEntity> accessableEntities = retrieveAccessableEntities();
        ArrayList<FormattedEntity> retCdpItems = new ArrayList<>();

        for (FormattedEntity accessableEntity : accessableEntities) {
            if (accessableEntity.hasGivenIds(givenEntityIds) == includable) {
                retCdpItems.add(accessableEntity);
            }
        }
        return retCdpItems;
    }

    public List<FormattedEntity> retrieveAccessableEntities() {
        List<FormattedEntity> entities = new ArrayList<>();
        entities.addAll(getJoinedChannels());
        entities.addAll(getGroups());
        entities.addAll(getFormattedUsers());
        return entities;
    }

    public FormattedEntity getEntityById(long entityId) {
        FormattedEntity topic = searchPublicTopicById(entityId);
        if (topic != null) {
            return topic;
        }
        FormattedEntity user = searchUserById(entityId);
        if (user != null) {
            return user;
        }
        FormattedEntity group = searchPrivateTopicById(entityId);
        if (group != null) {
            return group;
        }
        FormattedEntity bot = searchBotById(entityId);
        if (bot != null) {
            return bot;
        }

        return UNKNOWN_USER_ENTITY;
    }

    private FormattedEntity searchBotById(long entityId) {
        return this.bots.get(entityId);
    }

    public String getEntityNameById(long entityId) {
        FormattedEntity entity = getEntityById(entityId);
        return (entity != UNKNOWN_USER_ENTITY) ? entity.getName() : "";
    }

    public List<FormattedEntity> getUnjoinedMembersOfEntity(long entityId, int entityType) {
        FormattedEntity entity;
        if (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) {
            entity = searchPublicTopicById(entityId);
        } else if (entityType == JandiConstants.TYPE_PRIVATE_TOPIC) {
            entity = searchPrivateTopicById(entityId);
        } else {
            return new ArrayList<>();
        }
        if (entity != null) {
            return extractExclusivedUser(entity.getMembers());
        } else {
            return new ArrayList<>();
        }
    }

    private List<FormattedEntity> extractExclusivedUser(Collection<Long> joinedMembers) {
        List<FormattedEntity> ret = new ArrayList<>();

        Observable.merge(Observable.from(mStarredUsers.values()), Observable.from(mUsers.values()))
                .filter(formattedEntity ->
                                Observable.from(joinedMembers)
                                        .filter(entityRef -> entityRef == formattedEntity.getId())
                                        .map(entityRef -> false)
                                        .firstOrDefault(true)
                                        .toBlocking()
                                        .first()
                )
                .filter(formattedEntity -> !joinedMembers.contains(formattedEntity.getId()))
                .collect(() -> ret, (formattedEntities
                        , formattedEntity1) -> formattedEntities.add(formattedEntity1))
                .subscribe(formattedEntities1 -> {
                }, Throwable::printStackTrace);

        return ret;
    }

    public boolean isMyTopic(long entityId) {
        return isTopicOwner(entityId, mMe.id);
    }

    public boolean isTopicOwner(long topicId, long userId) {
        FormattedEntity searchedTopic = searchPublicTopicById(topicId);
        if (searchedTopic != null) {
            return searchedTopic.isMine(userId);
        }
        searchedTopic = searchPrivateTopicById(topicId);
        if (searchedTopic != null) {
            return searchedTopic.isMine(userId);
        }
        return false;
    }

    public boolean isMe(long userId) {
        return (getMe().getId() == userId);
    }

    private FormattedEntity searchPublicTopicById(long topicId) {
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

    private FormattedEntity searchPrivateTopicById(long groupId) {
        if (mStarredGroups.containsKey(groupId)) {
            return mStarredGroups.get(groupId);
        }
        if (mGroups.containsKey(groupId)) {
            return mGroups.get(groupId);
        }
        return null;
    }

    private FormattedEntity searchUserById(long userId) {
        if (mStarredUsers.containsKey(userId)) {
            return mStarredUsers.get(userId);
        }
        if (mUsers.containsKey(userId)) {
            return mUsers.get(userId);
        }
        return null;
    }

    /**
     * *********************************************************
     * 오름차순 정렬
     * **********************************************************
     */
    private List<FormattedEntity> sortFormattedEntityList(Collection<FormattedEntity> naiveEntities) {
        List<FormattedEntity> sortedEntities = new ArrayList<>(naiveEntities);
        Collections.sort(sortedEntities, (formattedEntity, formattedEntity2) ->
                StringCompareUtil.compare(formattedEntity.getName(), formattedEntity2.getName()));

        return sortedEntities;
    }

    public FormattedEntity getJandiBot() {
        return Observable.from(bots.values())
                .filter(botEntity -> TextUtils.equals(botEntity.getBotType(), "jandi_bot"))
                .toBlocking()
                .firstOrDefault(null);
    }

    public boolean hasJandiBot() {
        return Observable.from(bots.values())
                .filter(botEntity -> TextUtils.equals(botEntity.getBotType(), "jandi_bot"))
                .map(botEntity1 -> true)
                .toBlocking()
                .firstOrDefault(false);
    }

    public boolean isBot(long entityId) {
        return bots.containsKey(entityId);
    }

    public boolean isJandiBot(long entityId) {
        return hasJandiBot() && getJandiBot().getId() == entityId;
    }
}
