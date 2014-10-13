package com.tosslab.jandi.app.lists.entities;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class EntityManager {
    private final Logger log = Logger.getLogger(EntityManager.class);

    private ResLeftSideMenu.Team mMyTeam;
    private ResLeftSideMenu.User mMe;   // with MessageMarker
    private List<FormattedEntity> mJoinedChannels;
    private List<FormattedEntity> mUnJoinedChannels;
    private List<FormattedEntity> mUsers;
    private List<FormattedEntity> mPrivateGroups;

    public EntityManager(ResLeftSideMenu resLeftSideMenu) {
        mJoinedChannels = new ArrayList<FormattedEntity>();
        mUnJoinedChannels = new ArrayList<FormattedEntity>();
        mUsers = new ArrayList<FormattedEntity>();
        mPrivateGroups = new ArrayList<FormattedEntity>();

        this.mMyTeam = resLeftSideMenu.team;
        this.mMe = resLeftSideMenu.user;
        arrangeEntities(resLeftSideMenu);
    }

    private void arrangeEntities(ResLeftSideMenu resLeftSideMenu) {
        // Joined Channel 혹은 PrivateGroup 리스트 정리
        for (ResLeftSideMenu.Entity entity : resLeftSideMenu.joinEntities) {
            if (entity instanceof ResLeftSideMenu.Channel) {
                ResLeftSideMenu.Channel channel = (ResLeftSideMenu.Channel) entity;
                log.debug("Joined channel : " + channel.name
                        + ", owned by " + channel.ch_creatorId
                        + ", id : " + channel.id);

                addInJoinedChannels(channel);
            } else if (entity instanceof ResLeftSideMenu.PrivateGroup) {
                ResLeftSideMenu.PrivateGroup privateGroup = (ResLeftSideMenu.PrivateGroup) entity;
                mPrivateGroups.add(new FormattedEntity(privateGroup, mMe.u_messageMarkers));
            } else {
                // TODO : Error 처리
            }
        }

        // Unjoined channel 혹은 User 리스트 정리
        for (ResLeftSideMenu.Entity entity : resLeftSideMenu.entities) {
            if (entity instanceof ResLeftSideMenu.Channel) {
                addInUnjoinedChannels((ResLeftSideMenu.Channel) entity);
            } else if (entity instanceof ResLeftSideMenu.User) {
                mUsers.add(new FormattedEntity((ResLeftSideMenu.User) entity, mMe.u_messageMarkers));
            } else {
                // TODO : Error 처리
            }
        }
    }

    private int searchDuplicatedChannelPosition(List<FormattedEntity> targets, int channelId) {
        int ret = 0;
        for (FormattedEntity target : targets) {
            if (target.getChannel().id == channelId) {
                return ret;
            }
            ret++;
        }
        return -1;
    }

    private void removeDuplicatedEntityInUnjoinedChannels(ResLeftSideMenu.Channel channel) {
        int position = searchDuplicatedChannelPosition(mUnJoinedChannels, channel.id);
        if (position > -1) {
            mUnJoinedChannels.remove(position);
        }
    }

    private void addInJoinedChannels(ResLeftSideMenu.Channel channel) {
        // 만약 Unjoined 채널 부분에 이 항목이 존재한다면 그 항목을 삭제한다.
        removeDuplicatedEntityInUnjoinedChannels(channel);
        mJoinedChannels.add(new FormattedEntity(channel, FormattedEntity.JOINED, mMe.u_messageMarkers));
    }

    private void addInUnjoinedChannels(ResLeftSideMenu.Channel channel) {
        // 만약 Join 된 채널에 이 항목이 존재한다면 추가하지 않는다.
        int position = searchDuplicatedChannelPosition(mJoinedChannels, channel.id);
        if (position == -1) {
            mUnJoinedChannels.add(new FormattedEntity(channel, FormattedEntity.UNJOINED, mMe.u_messageMarkers));
        }
    }


    /************************************************************
     * Getter
     ************************************************************/
    public List<FormattedEntity> getJoinedChannels() {
        return mJoinedChannels;
    }

    public List<FormattedEntity> getUnjoinedChannels() {
        return mUnJoinedChannels;
    }

    public List<FormattedEntity> getFormattedPrivateGroups() {
        return mPrivateGroups;
    }

    public List<FormattedEntity> getFormattedUsers() {
        return mUsers;
    }

    public List<FormattedEntity> getFormattedUsersWithoutMe() {
        ArrayList<FormattedEntity> usersWithoutMe = new ArrayList<FormattedEntity>();
        for (FormattedEntity user : mUsers) {
            if (user.getUser().id != mMe.id) {
                usersWithoutMe.add(user);
            }
        }
        return  usersWithoutMe;
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
    public List<FormattedEntity> retrieveExceptGivenEntities(List<Integer> givenEntityIds) {
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
        entities.addAll(mJoinedChannels);
        entities.addAll(mPrivateGroups);
        entities.addAll(mUsers);
        return entities;
    }

    public FormattedEntity getEntityById(int entityId) {
        for (FormattedEntity target : mJoinedChannels) {
            if (target.getChannel().id == entityId) {
                return target;
            }
        }
        for (FormattedEntity target : mUsers) {
            if (target.getUser().id == entityId) {
                return target;
            }
        }
        for (FormattedEntity target : mPrivateGroups) {
            if (target.getPrivateGroup().id == entityId) {
                return target;
            }
        }
        for (FormattedEntity target : mUnJoinedChannels) {
            if (target.getChannel().id == entityId) {
                return target;
            }
        }
        return null;
    }

    public String getEntityNameById(int cdpId) {
        FormattedEntity entity = getEntityById(cdpId);
        return (entity != null) ? entity.toString() : "";
    }

    public List<FormattedEntity> getUnjoinedMembersOfEntity(int entityId, int entityType) {
        FormattedEntity entity;
        if (entityType == JandiConstants.TYPE_TOPIC) {
            entity = searchChannelById(entityId);
        } else if (entityType == JandiConstants.TYPE_GROUP) {
            entity = searchPrivateGroupById(entityId);
        } else {
            return null;
        }
        return getUnjoinedMembersOfEntity(entity.getMembers());
    }

    private List<FormattedEntity> getUnjoinedMembersOfEntity(List<Integer> joinedMembers) {
        ArrayList<FormattedEntity> unjoinedMemebers = new ArrayList<FormattedEntity>(mUsers);
        for (int id : joinedMembers) {
            for (int i = 0; i < unjoinedMemebers.size(); i++) {
                FormattedEntity user = unjoinedMemebers.get(i);
                if (user.getUser().id == id) {
                    unjoinedMemebers.remove(i);
                    break;
                }
            }
        }
        return unjoinedMemebers;
    }

    public boolean isMyEntity(int entityId) {
        FormattedEntity searchedEntity = searchChannelById(entityId);
        if (searchedEntity != null) {
            return searchedEntity.isMine(mMe.id);
        }

        return false;
    }

    public boolean isMe(int userId) {
        return (getMe().getId() == userId);
    }

    private FormattedEntity searchChannelById(int channelId) {
        for (FormattedEntity target : mJoinedChannels) {
            if (target.getChannel().id == channelId) {
                return target;
            }
        }
        for (FormattedEntity target : mUnJoinedChannels) {
            if (target.getChannel().id == channelId) {
                return target;
            }
        }
        return null;
    }

    private FormattedEntity searchPrivateGroupById(int privateGroupId) {
        for (FormattedEntity target : mPrivateGroups) {
            if (target.getPrivateGroup().id == privateGroupId) {
                return target;
            }
        }
        return null;
    }
}
