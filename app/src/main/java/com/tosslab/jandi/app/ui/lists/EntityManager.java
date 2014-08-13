package com.tosslab.jandi.app.ui.lists;

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
    private List<ResLeftSideMenu.Channel> mJoinedChannels;
    private List<ResLeftSideMenu.Channel> mUnJoinedChannels;
    private List<ResLeftSideMenu.User> mUsers;
    private List<ResLeftSideMenu.PrivateGroup> mPrivateGroups;

    public EntityManager(ResLeftSideMenu resLeftSideMenu) {
        mJoinedChannels = new ArrayList<ResLeftSideMenu.Channel>();
        mUnJoinedChannels = new ArrayList<ResLeftSideMenu.Channel>();
        mUsers = new ArrayList<ResLeftSideMenu.User>();
        mPrivateGroups = new ArrayList<ResLeftSideMenu.PrivateGroup>();

        arrangeEntities(resLeftSideMenu);
    }

    public List<FormattedEntity> getFormattedChannels() {
        List<FormattedEntity> formattedEntities = new ArrayList<FormattedEntity>();
        formattedEntities.add(new FormattedEntity(FormattedEntity.TYPE_TITLE_JOINED_CHANNEL));
        for (ResLeftSideMenu.Channel channel : mJoinedChannels) {
            formattedEntities.add(new FormattedEntity(channel, FormattedEntity.JOINED));
        }
        formattedEntities.add(new FormattedEntity(FormattedEntity.TYPE_TITLE_UNJOINED_CHANNEL));
        for (ResLeftSideMenu.Channel channel : mUnJoinedChannels) {
            formattedEntities.add(new FormattedEntity(channel, FormattedEntity.UNJOINED));
        }
        return formattedEntities;
    }

    public List<ResLeftSideMenu.User> getUsers() {
        return mUsers;
    }

    public List<FormattedEntity> getFormattedPrivateGroups() {
        List<FormattedEntity> formattedEntities = new ArrayList<FormattedEntity>();
        for (ResLeftSideMenu.PrivateGroup privateGroup : mPrivateGroups) {
            formattedEntities.add(new FormattedEntity(privateGroup));
        }
        return formattedEntities;
    }

    private int searchDuplicatedPosition(List<ResLeftSideMenu.Channel> targets, int channelId) {
        int ret = 0;
        for (ResLeftSideMenu.Channel target : targets) {
            if (target.id == channelId) {
                return ret;
            }
            ret++;
        }
        return -1;
    }

    private void removeDuplicatedEntityInUnjoinedChannels(ResLeftSideMenu.Channel channel) {
        int position = searchDuplicatedPosition(mUnJoinedChannels, channel.id);
        if (position > -1) {
            mUnJoinedChannels.remove(position);
        }
    }

    private void addInJoinedChannels(ResLeftSideMenu.Channel channel) {
        // 만약 Unjoined 채널 부분에 이 항목이 존재한다면 그 항목을 삭제한다.
        removeDuplicatedEntityInUnjoinedChannels(channel);
        mJoinedChannels.add(channel);
    }

    private void addInUnjoinedChannels(ResLeftSideMenu.Channel channel) {
        // 만약 Join 된 채널에 이 항목이 존재한다면 추가하지 않는다.
        int position = searchDuplicatedPosition(mJoinedChannels, channel.id);
        if (position == -1) {
            mUnJoinedChannels.add(channel);
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
                mPrivateGroups.add((ResLeftSideMenu.PrivateGroup) entity);
            } else {
                // TODO : Error 처리
            }
        }

        // Unjoined channel 혹은 User 리스트 정리
        for (ResLeftSideMenu.Entity entity : resLeftSideMenu.entities) {
            if (entity instanceof ResLeftSideMenu.Channel) {
                addInUnjoinedChannels((ResLeftSideMenu.Channel) entity);
            } else if (entity instanceof ResLeftSideMenu.User) {
                mUsers.add((ResLeftSideMenu.User) entity);
            } else {
                // TODO : Error 처리
            }
        }
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

    private List<ResLeftSideMenu.User> getUsersWithoutMe() {
        ArrayList<ResLeftSideMenu.User> usersWithoutMe = new ArrayList<ResLeftSideMenu.User>();
        for (ResLeftSideMenu.User user : mUsers) {
            if (user.id != mMe.id) {
                usersWithoutMe.add(user);
            }
        }
        return  usersWithoutMe;
    }

    // TODO 현재는 default channel이 그냥 첫번째 채널
    public ResLeftSideMenu.Channel getDefaultChannel() {
        return mJoinedChannels.get(0);
    }

    // 현재 멤버들 중에서 선택한 Channel에 속하지 않은 멤버를 반환
    public List<ResLeftSideMenu.User> getUnjoinedMembersOfChannel(ResLeftSideMenu.Channel choosenChannel) {
        return getUnjoinedMembersOfEntity(choosenChannel.ch_members);
    }

    // 현재 멤버들 중에서 선택한 PrivateGroup에 속하지 않은 멤버를 반환
    public List<ResLeftSideMenu.User> getUnjoinedMembersOfPrivateGroup(ResLeftSideMenu.PrivateGroup choosenGroup) {
        return getUnjoinedMembersOfEntity(choosenGroup.pg_members);
    }

    private List<ResLeftSideMenu.User> getUnjoinedMembersOfEntity(List<Integer> joinedMembers) {
        ArrayList<ResLeftSideMenu.User> unjoinedMemebers = new ArrayList<ResLeftSideMenu.User>(mUsers);
        for (int id : joinedMembers) {
            for (int i = 0; i < unjoinedMemebers.size(); i++) {
                ResLeftSideMenu.User user = unjoinedMemebers.get(i);
                if (user.id == id) {
                    unjoinedMemebers.remove(i);
                    break;
                }
            }
        }
        return unjoinedMemebers;
    }

//    public CdpItem getCdpItemById(int cdpId) {
//        for (CdpItem target : mJoinedChannels) {
//            if (target.id == cdpId) {
//                return target;
//            }
//        }
//        for (CdpItem target : mUsers) {
//            if (target.id == cdpId) {
//                return target;
//            }
//        }
//        for (CdpItem target : mPrivateGroups) {
//            if (target.id == cdpId) {
//                return target;
//            }
//        }
//        return null;
//    }

//    public String getCdpNameById(int cdpId) {
//        CdpItem cdpItem = getCdpItemById(cdpId);
//        return (cdpItem != null) ? cdpItem.toString() : "";
//    }
}
