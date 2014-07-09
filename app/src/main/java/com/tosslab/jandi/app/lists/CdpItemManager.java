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
    private List<CdpItem> mUsers;
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
                , mUnJoinedChannels.size() - mJoinedChannels.size()));
        cdpItems.add(new CdpItem(JandiConstants.TYPE_TITLE_DIRECT_MESSAGE));
        cdpItems.addAll(mUsers);
        cdpItems.add(new CdpItem(JandiConstants.TYPE_TITLE_PRIVATE_GROUP));
        cdpItems.addAll(mPrivateGroups);

        return cdpItems;
    }

    public List<CdpItem> retrieveWithoutTitle() {
        ArrayList<CdpItem> cdpItems = new ArrayList<CdpItem>();

        // 리스트에서 제목으로 처리할 item을 추가한다.
        cdpItems.addAll(mJoinedChannels);
        cdpItems.addAll(mUsers);
        cdpItems.addAll(mPrivateGroups);

        return cdpItems;
    }

    private void arrangeCdpItems(ResLeftSideMenu resLeftSideMenu) {
        this.mMe = resLeftSideMenu.user;
        // Joined Channel 혹은 PrivateGroup 리스트 정리
        for (ResLeftSideMenu.Entity entity : resLeftSideMenu.joinEntity) {
            if (entity instanceof ResLeftSideMenu.Channel) {
                // 만약 Unjoined 채널 부분에 이 항목이 존재한다면 그 항목을 삭제한다.
                CdpItem cdpItem = new CdpItem((ResLeftSideMenu.Channel) entity);
                log.debug("Joined entity : " + entity.name + ", owned by " + ((ResLeftSideMenu.Channel)entity).ch_creatorId + ", id : " + entity.id);
                int position = mUnJoinedChannels.indexOf(cdpItem);
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
                if (mJoinedChannels.indexOf(cdpItem) < 0) {
                    mUnJoinedChannels.add(cdpItem);
                }
            } else if (entity instanceof ResLeftSideMenu.User) {
                if (entity.id != mMe.id)
                    mUsers.add(new CdpItem((ResLeftSideMenu.User) entity));
            } else {
                // TODO : Error 처리
            }
        }
    }
}
