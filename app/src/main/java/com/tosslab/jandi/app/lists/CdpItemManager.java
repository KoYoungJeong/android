package com.tosslab.jandi.app.lists;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 6. 17..
 * 사용자가 가지고 있는 Channel, User, Private Groups 의 정보 저장
 * Joined, Unjoined 구분
 */
public class CdpItemManager {
    private final Logger log = Logger.getLogger(CdpItemManager.class);

    public ResLeftSideMenu.User mMe;
    private List<CdpItem> mJoinedChannels;
    public List<CdpItem> mUnJoinedChannels;
    public List<CdpItem> mUsers;
    private List<CdpItem> mPrivateGroups;

    public CdpItemManager(ResLeftSideMenu resLeftSideMenu) {
        mJoinedChannels = new ArrayList<CdpItem>();
        mUnJoinedChannels = new ArrayList<CdpItem>();
        mUsers = new ArrayList<CdpItem>();
        mPrivateGroups = new ArrayList<CdpItem>();

        arrangeCdpItems(resLeftSideMenu);
    }

    public List<CdpItem> retrieve() {
        ArrayList<CdpItem> cdpItems = new ArrayList<CdpItem>();

        // 리스트에서 제목으로 처리할 item을 추가한다.
        cdpItems.add(new CdpItem(JandiConstants.TYPE_TITLE_JOINED_CHANNEL));
        cdpItems.addAll(mJoinedChannels);
        cdpItems.add(new CdpItem(JandiConstants.TYPE_TITLE_UNJOINED_CHANNEL
                , mUnJoinedChannels.size()));
        cdpItems.add(new CdpItem(JandiConstants.TYPE_TITLE_DIRECT_MESSAGE));
        cdpItems.addAll(getUsersWithoutMe());
        cdpItems.add(new CdpItem(JandiConstants.TYPE_TITLE_PRIVATE_GROUP));
        cdpItems.addAll(mPrivateGroups);

        return cdpItems;
    }

    public List<CdpItem> retrieveWithoutTitle() {
        ArrayList<CdpItem> cdpItems = new ArrayList<CdpItem>();

        cdpItems.addAll(mJoinedChannels);
        cdpItems.addAll(getUsersWithoutMe());
        cdpItems.addAll(mPrivateGroups);

        return cdpItems;
    }

    private List<CdpItem> getUsersWithoutMe() {
        ArrayList<CdpItem> usersWithoutMe = new ArrayList<CdpItem>();
        for (CdpItem user : mUsers) {
            if (user.id != mMe.id) {
                usersWithoutMe.add(user);
            }
        }
        return  usersWithoutMe;
    }

    // TODO 현재는 default channel이 그냥 첫번째 채널
    public CdpItem getDefaultChannel() {
        return mJoinedChannels.get(0);
    }

    // 현재 멤버들 중에서 선택한 Channel 혹은 PG에 속하지 않은 멤버를 반환
    public List<CdpItem> getUnjoinedMembersByChoosenCdp(CdpItem choosenCdp) {
        ArrayList<CdpItem> unjoinedMemebers = new ArrayList<CdpItem>(mUsers);
        for (int id : choosenCdp.joinedMember) {
            for (int i = 0; i < unjoinedMemebers.size(); i++) {
                CdpItem user = unjoinedMemebers.get(i);
                if (user.id == id) {
                    unjoinedMemebers.remove(i);
                    break;
                }
            }
        }
        return unjoinedMemebers;
    }

    public CdpItem getCdpItemById(int cdpId) {
        for (CdpItem target : mJoinedChannels) {
            if (target.id == cdpId) {
                return target;
            }
        }
        for (CdpItem target : mUsers) {
            if (target.id == cdpId) {
                return target;
            }
        }
        for (CdpItem target : mPrivateGroups) {
            if (target.id == cdpId) {
                return target;
            }
        }
        return null;
    }

    private int searchPosition(List<CdpItem> targets, CdpItem item) {
        int ret = 0;
        for (CdpItem target : targets) {
            if (target.id == item.id) {
                return ret;
            }
            ret++;
        }
        return -1;
    }

    private void arrangeCdpItems(ResLeftSideMenu resLeftSideMenu) {
        this.mMe = resLeftSideMenu.user;
        // Joined Channel 혹은 PrivateGroup 리스트 정리
        for (ResLeftSideMenu.Entity entity : resLeftSideMenu.joinEntity) {
            if (entity instanceof ResLeftSideMenu.Channel) {

                CdpItem cdpItem = new CdpItem((ResLeftSideMenu.Channel) entity);
                log.debug("Joined entity : " + entity.name + ", owned by " + ((ResLeftSideMenu.Channel)entity).ch_creatorId + ", id : " + entity.id);

                // 만약 Unjoined 채널 부분에 이 항목이 존재한다면 그 항목을 삭제한다.
                int position = searchPosition(mUnJoinedChannels, cdpItem);
                if (position > -1) {
                    mUnJoinedChannels.remove(position);
                }
                mJoinedChannels.add(cdpItem);
            } else if (entity instanceof ResLeftSideMenu.PrivateGroup) {
                mPrivateGroups.add(new CdpItem((ResLeftSideMenu.PrivateGroup) entity));
            } else {
                // TODO : Error 처리
            }
        }

        // Unjoined channel 혹은 User 리스트 정리
        for (ResLeftSideMenu.Entity entity : resLeftSideMenu.entities) {
            if (entity instanceof ResLeftSideMenu.Channel) {
                // 만약 Join 된 채널에 이 항목이 존재한다면 추가하지 않는다.
                CdpItem cdpItem = new CdpItem((ResLeftSideMenu.Channel) entity);

                int position = searchPosition(mJoinedChannels, cdpItem);
                if (position == -1) {
                    mUnJoinedChannels.add(cdpItem);
                }
            } else if (entity instanceof ResLeftSideMenu.User) {
                mUsers.add(new CdpItem((ResLeftSideMenu.User) entity));
            } else {
                // TODO : Error 처리
            }
        }
    }
}
