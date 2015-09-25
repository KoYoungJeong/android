package com.tosslab.jandi.app.ui.share.views.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.network.models.ResFolderItem;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;
import com.tosslab.jandi.app.ui.share.views.domain.ExpandRoomData;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by tee on 15. 9. 15..
 */
@EBean
public class ShareSelectModel {

    private Map<Integer, FormattedEntity> mJoinedTopics;
    private Map<Integer, FormattedEntity> mUnjoinedTopics;
    private Map<Integer, FormattedEntity> mUsers;
    private Map<Integer, FormattedEntity> mJoinedUsers;
    private Map<Integer, FormattedEntity> mGroups;

    private Map<Integer, FormattedEntity> mStarredJoinedTopics;
    private Map<Integer, FormattedEntity> mStarredUsers;
    private Map<Integer, FormattedEntity> mStarredGroups;
    private ResLeftSideMenu.Team currentTeam;
    private ResLeftSideMenu.User mMe;


    public void initFormattedEntities(int teamId) {

        ResLeftSideMenu resLeftSideMenu = RequestApiManager.getInstance()
                .getInfosForSideMenuByMainRest(teamId);

        mJoinedTopics = new HashMap<>();
        mUnjoinedTopics = new HashMap<>();
        mUsers = new HashMap<>();
        mJoinedUsers = new HashMap<>();
        mGroups = new HashMap<>();
        mStarredJoinedTopics = new HashMap<>();
        mStarredUsers = new HashMap<>();
        mStarredGroups = new HashMap<>();

        this.mMe = resLeftSideMenu.user;
        this.currentTeam = resLeftSideMenu.team;

        Collection<Integer> starredEntities =
                (resLeftSideMenu.user.u_starredEntities != null)
                        ? resLeftSideMenu.user.u_starredEntities : new ArrayList<Integer>();

        for (ResLeftSideMenu.Entity entity : resLeftSideMenu.entities) {
            if (entity instanceof ResLeftSideMenu.Channel) {
                FormattedEntity unjoinedTopic
                        = new FormattedEntity((ResLeftSideMenu.Channel) entity, FormattedEntity.UNJOINED);
                mUnjoinedTopics.put(entity.id, unjoinedTopic);

            } else if (entity instanceof ResLeftSideMenu.User) {
                FormattedEntity user = new FormattedEntity((ResLeftSideMenu.User) entity);
                if (starredEntities.contains(entity.id)) {
                    user.isStarred = true;
                    mStarredUsers.put(entity.id, user);
                } else {
                    mUsers.put(entity.id, user);
                }
            }
        }

        for (ResLeftSideMenu.Entity entity : resLeftSideMenu.joinEntities) {
            if (entity instanceof ResLeftSideMenu.Channel) {
                // 만일 unjoined topic 에 해당 topic 이 있다면 뺀다.
                mUnjoinedTopics.remove(entity.id);
                FormattedEntity joinedTopic
                        = new FormattedEntity((ResLeftSideMenu.Channel) entity, FormattedEntity.JOINED);
                if (starredEntities.contains(entity.id)) {
                    joinedTopic.isStarred = true;
                    mStarredJoinedTopics.put(entity.id, joinedTopic);
                } else {
                    mJoinedTopics.put(entity.id, joinedTopic);
                }

            } else if (entity instanceof ResLeftSideMenu.PrivateGroup) {
                ResLeftSideMenu.PrivateGroup privateGroup = (ResLeftSideMenu.PrivateGroup) entity;
                FormattedEntity group = new FormattedEntity(privateGroup);
                if (starredEntities.contains(entity.id)) {
                    group.isStarred = true;
                    mStarredGroups.put(entity.id, group);
                } else {
                    mGroups.put(entity.id, group);
                }
            } else if (entity instanceof ResLeftSideMenu.User) {
                ResLeftSideMenu.User user = (ResLeftSideMenu.User) entity;
                mJoinedUsers.put(user.id, new FormattedEntity(user));
            }
        }
    }

    public ResLeftSideMenu.Team getCurrentTeam() {
        return currentTeam;
    }

    // 폴더 정보 가져오기
    public List<ResFolder> getTopicFolders(int teamId) throws RetrofitError {
        return RequestApiManager.getInstance().getFoldersByTeamApi(teamId);
    }

    // 폴더 속 토픽 아이디 가져오기
    public List<ResFolderItem> getTopicFolderItems(int teamId) throws RetrofitError {
        return RequestApiManager.getInstance().getFolderItemsByTeamApi(teamId);
    }

    protected Observable<List<FormattedEntity>> getUsers() {
        return Observable.from(getFormattedUsersWithoutMe())
                .filter(formattedEntity -> TextUtils.equals(formattedEntity.getUser().status, "enabled"))
                .toSortedList((lhs, rhs) -> {
                    return lhs.getName().compareToIgnoreCase(rhs.getName());
                });
    }

    public List<ExpandRoomData> getTopicDatas(int teamId) {

        List<ResFolder> topicFolders = getTopicFolders(teamId);
        List<ResFolderItem> topicFolderItems = getTopicFolderItems(teamId);
        List<ExpandRoomData> topicDatas = new ArrayList<>();
        LinkedHashMap<Integer, FormattedEntity> joinTopics = getJoinEntities();

        LinkedHashMap<Integer, List<ExpandRoomData>> topicDataMap = new LinkedHashMap<>();

        for (ResFolder topicFolder : topicFolders) {
            if (!topicDataMap.containsKey(topicFolder.id)) {
                topicDataMap.put(new Integer(topicFolder.id), new ArrayList<>());
            }
        }

        Observable.from(topicFolderItems)
                .filter(item -> item.folderId > 0)
                .subscribe(item -> {
                    ExpandRoomData topicData = new ExpandRoomData();
                    FormattedEntity topic = joinTopics.get(item.roomId);
                    joinTopics.remove(item.roomId);
                    topicData.setEntityId(item.roomId);
                    topicData.setIsUser(false);
                    topicData.setName(topic.getName());
                    topicData.setType(topic.type);
                    topicData.setIsFolder(false);
                    topicData.setIsPublicTopic(topic.isPublicTopic());
                    topicData.setIsStarred(topic.isStarred);
                    topicDataMap.get(new Integer(item.folderId)).add(topicData);
                });

        for (ResFolder folder : topicFolders) {
            Collections.sort(topicDataMap.get(new Integer(folder.id)), (lhs, rhs) -> {
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
        }

        // 각 폴더와 종속된 토픽 데이터 셋팅
        for (ResFolder folder : topicFolders) {
            ExpandRoomData folderdata = new ExpandRoomData();
            folderdata.setIsFolder(true);
            folderdata.setIsUser(false);
            folderdata.setName(folder.name);
            topicDatas.add(folderdata);
            for (ExpandRoomData roomData : topicDataMap.get(new Integer(folder.id))) {
                topicDatas.add(roomData);
            }
        }

        Iterator joinTopicKeySets = joinTopics.keySet().iterator();

        boolean FirstAmongNoFolderItem = true;

        while (joinTopicKeySets.hasNext()) {
            FormattedEntity entity = joinTopics.get(joinTopicKeySets.next());
            ExpandRoomData topicData = new ExpandRoomData();
            topicData.setIsFirstAmongNoFolderItem(FirstAmongNoFolderItem);
            FirstAmongNoFolderItem = false;
            topicData.setEntityId(entity.getId());
            topicData.setIsUser(false);
            topicData.setName(entity.getName());
            topicData.setType(entity.type);
            topicData.setIsFolder(false);
            topicData.setIsPublicTopic(entity.isPublicTopic());
            topicData.setIsStarred(entity.isStarred);

            topicDatas.add(topicData);
        }

        LogUtil.e("topicDatas", topicDatas.size() + "");

        return topicDatas;
    }

    public LinkedHashMap<Integer, FormattedEntity> getJoinEntities() {
        List<FormattedEntity> joinedChannels = getJoinedChannels();
        List<FormattedEntity> groups = getGroups();
        LinkedHashMap topicHashMap = new LinkedHashMap<Integer, Topic>();

        Observable<FormattedEntity> observable = Observable.merge(Observable.from(joinedChannels), Observable.from(groups));

        observable.toSortedList((lhs, rhs) -> {

            if (lhs.isStarred && rhs.isStarred) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            } else if (lhs.isStarred) {
                return -1;
            } else if (rhs.isStarred) {
                return 1;
            } else {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }

        }).subscribe(topics -> {
            for (FormattedEntity topic : topics) {
                topicHashMap.put(topic.getId(), topic);
            }
        });

        return topicHashMap;
    }

    public ArrayList<ExpandRoomData> getUserRoomDatas() {
        ArrayList<ExpandRoomData> dmDatas = new ArrayList<>();
        getUsers().subscribe(entities -> {
            for (FormattedEntity entity : entities) {
                ExpandRoomData userData = new ExpandRoomData();

                userData.setIsUser(true);
                userData.setName(entity.getName());
                try {
                    userData.setProfileUrl(entity.getUserSmallProfileUrl());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                userData.setType(entity.type);
                userData.setEntityId(entity.getId());
                userData.setIsStarred(entity.isStarred);
                userData.setIsFolder(false);
                dmDatas.add(userData);
            }
        });
        return dmDatas;
    }

    public List<FormattedEntity> getJoinedChannels() {
        ArrayList<FormattedEntity> ret = new ArrayList<>();
        ret.addAll(mStarredJoinedTopics.values());
        ret.addAll(sortFormattedEntityList(mJoinedTopics.values()));
        return ret;
    }

    public List<FormattedEntity> getGroups() {
        ArrayList<FormattedEntity> ret = new ArrayList<>();
        ret.addAll(mStarredGroups.values());
        ret.addAll(sortFormattedEntityList(mGroups.values()));
        return ret;
    }

    private List<FormattedEntity> sortFormattedEntityList(Collection<FormattedEntity> naiveEntities) {
        List<FormattedEntity> sortedEntities = new ArrayList<>(naiveEntities);
        Collections.sort(sortedEntities, (formattedEntity, formattedEntity2) ->
                formattedEntity.getName().compareToIgnoreCase(formattedEntity2.getName()));

        return sortedEntities;
    }

    public List<FormattedEntity> getFormattedUsersWithoutMe() {
        ArrayList<FormattedEntity> ret = new ArrayList<>();

        if (mStarredUsers != null) {
            Observable.from(mStarredUsers.values())
                    .filter(formattedEntity -> formattedEntity.getId() != mMe.id)
                    .collect(() -> ret, (o, formattedEntity1) -> o.add(formattedEntity1))
                    .subscribe();
        }

        List<FormattedEntity> tempEntities = new ArrayList<>();

        Observable.from(mUsers.values())
                .filter(formattedEntity -> formattedEntity.getId() != mMe.id)
                .collect(() -> tempEntities, (o, formattedEntity1) -> o.add(formattedEntity1))
                .subscribe();

        ret.addAll(sortFormattedEntityList(tempEntities));

        return ret;
    }

    public List<Team> getTeamInfos() throws RetrofitError {

        ArrayList<Team> teams = new ArrayList<>();

        List<ResAccountInfo.UserTeam> userTeams = AccountRepository.getRepository().getAccountTeams();

        teams.addAll(convertJoinedTeamList(userTeams));

        List<ResPendingTeamInfo> pendingTeamInfo = RequestApiManager.getInstance().getPendingTeamInfoByInvitationApi();
        for (int idx = pendingTeamInfo.size() - 1; idx >= 0; idx--) {
            if (!TextUtils.equals(pendingTeamInfo.get(idx).getStatus(), "pending")) {
                pendingTeamInfo.remove(idx);
            }
        }

        teams.addAll(convertPedingTeamList(pendingTeamInfo));

        return teams;
    }

    private List<Team> convertPedingTeamList(List<ResPendingTeamInfo> pedingTeamInfos) {
        List<Team> teams = new ArrayList<Team>();

        if (pedingTeamInfos == null) {
            return teams;
        }

        for (ResPendingTeamInfo pedingTeamInfo : pedingTeamInfos) {
            teams.add(Team.createTeam(pedingTeamInfo));
        }

        return teams;
    }

    private List<Team> convertJoinedTeamList(List<ResAccountInfo.UserTeam> memberships) {
        List<Team> teams = new ArrayList<Team>();

        if (memberships == null) {
            return teams;
        }

        for (ResAccountInfo.UserTeam membership : memberships) {
            teams.add(Team.createTeam(membership));
        }

        return teams;
    }

    public ResAccountInfo.UserTeam getSelectedTeamInfo() {
        return AccountRepository.getRepository().getSelectedTeamInfo();
    }

}