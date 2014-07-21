package com.tosslab.jandi.app.lists;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 5. 27..
 * 왼쪽 사이드 메뉴에 위치할 ListView의 Channel, User, PrivateGroup 리스트 아이템.
 */
public class CdpItem {
    public final int ownerId;
    public final String name;
    public final int type;
    public final int id;
    protected final String profileUrl;      // cdp type이 사용자라면 존재
    public final List<Integer> joinedMember; // CDP type이 Channel이나 PG라면...
    public boolean isSelected = false;

    public CdpItem(int typeOfTitle) {
        this.name = null;
        this.id = -1;
        this.ownerId = -1;
        this.type = typeOfTitle;
        this.profileUrl = null;
        this.joinedMember = null;
    }

    public CdpItem(int typeOfTitle, int units) {
        this.name = "+ " + units + " mores...";
        this.id = -1;
        this.ownerId = -1;
        this.type = typeOfTitle;
        this.profileUrl = null;
        this.joinedMember = null;
    }

    public CdpItem(ResLeftSideMenu.Channel channel) {
        this.name = channel.name;
        this.type = JandiConstants.TYPE_CHANNEL;
        this.id = channel.id;
        this.ownerId = channel.ch_creatorId;
        this.profileUrl = null;
        this.joinedMember = channel.ch_members;
    }

    public CdpItem(ResLeftSideMenu.User user) {
        this.name = user.name;
        this.type = JandiConstants.TYPE_DIRECT_MESSAGE;
        this.id = user.id;
        this.ownerId = user.id;
        this.profileUrl = JandiConstants.SERVICE_ROOT_URL + user.u_photoUrl;
        this.joinedMember = null;
    }

    public CdpItem(ResLeftSideMenu.PrivateGroup pGroup) {
        this.name = pGroup.name;
        this.type = JandiConstants.TYPE_PRIVATE_GROUP;
        this.id = pGroup.id;
        this.ownerId = pGroup.pg_creatorId;
        this.profileUrl = null;
        this.joinedMember = pGroup.pg_members;
    }

    public boolean hasGivenId(int cdpId) {
        return (this.id == cdpId);
    }

    public boolean hasGivenIds(List<Integer> cdpIds) {
        for (int cdpId : cdpIds) {
            if (hasGivenId(cdpId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        switch (this.type) {
            case JandiConstants.TYPE_CHANNEL:
                return "# " + this.name;
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return "@ " + this.name;
            default:
                return this.name;
        }
    }

    public String getProfileUrl() {
        return this.profileUrl;
    }
}
