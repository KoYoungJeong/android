package com.tosslab.jandi.app.ui.lists;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.CdpItem;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.models.FormattedEntity;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class EntityManager {
    private final Logger log = Logger.getLogger(EntityManager.class);

    private ResLeftSideMenu.User mMe;
    private List<FormattedEntity> mJoinedChannels;
    private List<FormattedEntity> mUnJoinedChannels;
    private List<FormattedEntity> mUsers;
    private List<FormattedEntity> mPrivateGroups;

    public EntityManager(ResLeftSideMenu resLeftSideMenu) {
        mJoinedChannels = new ArrayList<FormattedEntity>();
        mUnJoinedChannels = new ArrayList<FormattedEntity>();
        mUsers = new ArrayList<FormattedEntity>();
        mPrivateGroups = new ArrayList<FormattedEntity>();

        arrangeEntities(resLeftSideMenu);
    }

    public List<FormattedEntity> getFormattedChannels() {
        List<FormattedEntity> formattedEntities = new ArrayList<FormattedEntity>();
        formattedEntities.add(new FormattedEntity(FormattedEntity.TYPE_TITLE_JOINED_CHANNEL));
        formattedEntities.addAll(mJoinedChannels);
        formattedEntities.add(new FormattedEntity(FormattedEntity.TYPE_TITLE_UNJOINED_CHANNEL));
        formattedEntities.addAll(mUnJoinedChannels);
        return formattedEntities;
    }

    public List<FormattedEntity> getFormattedPrivateGroups() {
        return mPrivateGroups;
    }

    public List<ResLeftSideMenu.User> getUsers() {
        List<ResLeftSideMenu.User> users = new ArrayList<ResLeftSideMenu.User>();
        for (FormattedEntity userEntity : mUsers) {
            users.add(userEntity.getUser());
        }
        return users;
    }

    public List<ResLeftSideMenu.User> getUsersWithoutMe() {
        ArrayList<ResLeftSideMenu.User> usersWithoutMe = new ArrayList<ResLeftSideMenu.User>();
        for (FormattedEntity user : mUsers) {
            if (user.getUser().id != mMe.id) {
                usersWithoutMe.add(user.getUser());
            }
        }
        return  usersWithoutMe;
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
        mJoinedChannels.add(new FormattedEntity(channel, FormattedEntity.JOINED));
    }

    private void addInUnjoinedChannels(ResLeftSideMenu.Channel channel) {
        // 만약 Join 된 채널에 이 항목이 존재한다면 추가하지 않는다.
        int position = searchDuplicatedChannelPosition(mJoinedChannels, channel.id);
        if (position == -1) {
            mUnJoinedChannels.add(new FormattedEntity(channel, FormattedEntity.UNJOINED));
        }
    }

    private void arrangeEntities(ResLeftSideMenu resLeftSideMenu) {
        this.mMe = resLeftSideMenu.user;

        // Joined Channel 혹은 PrivateGroup 리스트 정리
        for (ResLeftSideMenu.Entity entity : resLeftSideMenu.joinEntity) {
            if (entity instanceof ResLeftSideMenu.Channel) {
                ResLeftSideMenu.Channel channel = (ResLeftSideMenu.Channel) entity;
                log.debug("Joined channel : " + channel.name
                        + ", owned by " + channel.ch_creatorId
                        + ", id : " + channel.id);

                addInJoinedChannels(channel);
            } else if (entity instanceof ResLeftSideMenu.PrivateGroup) {
                ResLeftSideMenu.PrivateGroup privateGroup = (ResLeftSideMenu.PrivateGroup) entity;
                mPrivateGroups.add(new FormattedEntity(privateGroup));
            } else {
                // TODO : Error 처리
            }
        }

        // Unjoined channel 혹은 User 리스트 정리
        for (ResLeftSideMenu.Entity entity : resLeftSideMenu.entities) {
            if (entity instanceof ResLeftSideMenu.Channel) {
                addInUnjoinedChannels((ResLeftSideMenu.Channel) entity);
            } else if (entity instanceof ResLeftSideMenu.User) {
                mUsers.add(new FormattedEntity((ResLeftSideMenu.User) entity));
            } else {
                // TODO : Error 처리
            }
        }
    }

    public FormattedEntity getMe() {
        return new FormattedEntity(mMe);
    }

    //
//    public List<CdpItem> retrieveAllEntities() {
//        ArrayList<CdpItem> cdpItems = new ArrayList<CdpItem>();
//
//        cdpItems.addAll(mJoinedChannels);
//        cdpItems.addAll(getUsersWithoutMe());
//        cdpItems.addAll(mPrivateGroups);
//
//        return cdpItems;
//    }
//
//    /**
//     * 인자로 주어진 ID에 해당하는 CDP를 추출한다.
//     * @param givenEntityIds
//     * @return
//     */
//    public List<CdpItem> retrieveGivenEntities(List<Integer> givenEntityIds) {
//        List<CdpItem> cdpItems = retrieveAllEntities();
//        ArrayList<CdpItem> retCdpItems = new ArrayList<CdpItem>();
//
//        for (CdpItem cdpItem : cdpItems) {
//            if (cdpItem.hasGivenIds(givenEntityIds)) {
//                retCdpItems.add(cdpItem);
//            }
//        }
//        return retCdpItems;
//    }
//
//    /**
//     * 인자로 주어진 ID 를 제외한 공유 대상 CDP를 추출한다.
//     * @param givenEntityIds
//     * @return
//     */
//    public List<CdpItem> retrieveExceptGivenEntities(List<Integer> givenEntityIds) {
//        List<CdpItem> cdpItems = retrieveWithoutTitle();
//        ArrayList<CdpItem> retCdpItems = new ArrayList<CdpItem>();
//
//        for (CdpItem cdpItem : cdpItems) {
//            if (!cdpItem.hasGivenIds(givenEntityIds)) {
//                retCdpItems.add(cdpItem);
//            }
//        }
//        return retCdpItems;
//    }

//    // TODO 현재는 default channel이 그냥 첫번째 채널
//    public ResLeftSideMenu.Channel getDefaultChannel() {
//        return mJoinedChannels.get(0);
//    }

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
        return null;
    }

    public String getEntityNameById(int cdpId) {
        FormattedEntity entity = getEntityById(cdpId);
        return (entity != null) ? entity.toString() : "";
    }

    public List<FormattedEntity> getUnjoinedMembersOfEntity(int entityId, int entityType) {
        FormattedEntity entity;
        if (entityType == JandiConstants.TYPE_CHANNEL) {
            entity = searchChannelById(entityId);
        } else if (entityType == JandiConstants.TYPE_PRIVATE_GROUP) {
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
        if (searchedEntity != null && searchedEntity.getChannel().ch_creatorId == mMe.id) {
            return true;
        }

        FormattedEntity searchedPrivateGroup = searchPrivateGroupById(entityId);
        if (searchedPrivateGroup != null && searchedPrivateGroup.getPrivateGroup().pg_creatorId == mMe.id) {
            return true;
        }
        return false;
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
