package com.tosslab.jandi.app.network;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.ReqCreateCdp;
import com.tosslab.jandi.app.network.models.ReqInviteUsers;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.JandiException;

import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 27..
 */
public class JandiEntityClient {
    private final String AUTH_HEADER = JandiConstants.AUTH_HEADER;
    private JandiRestClient mJandiRestClient;

    public JandiEntityClient(JandiRestClient jandiRestClient, String token) {
        mJandiRestClient = jandiRestClient;
        mJandiRestClient.setHeader(AUTH_HEADER, token);
    }

    public ResLeftSideMenu getTotalEntitiesInfo() throws JandiException {
        try {
            return mJandiRestClient.getInfosForSideMenu();
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
    }

    public ResCommon createChannel(String entityName) throws JandiException {
        ReqCreateCdp reqCreateCdp = new ReqCreateCdp();
        reqCreateCdp.name = entityName;
        try {
            return mJandiRestClient.createChannel(reqCreateCdp);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
    }

    public ResCommon createPrivateGroup(String entityName) throws JandiException {
        ReqCreateCdp reqCreateCdp = new ReqCreateCdp();
        reqCreateCdp.name = entityName;
        try {
            return mJandiRestClient.createPrivateGroup(reqCreateCdp);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
    }

    public ResCommon joinChannel(ResLeftSideMenu.Channel channel) throws JandiException {
        try {
            return mJandiRestClient.joinChannel(channel.id);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
    }

    public ResCommon leaveChannel(int id) throws JandiException {
        try {
            return mJandiRestClient.leaveChannel(id);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
    }

    public ResCommon leavePrivateGroup(int id) throws JandiException {
        try {
            return mJandiRestClient.leaveGroup(id);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
    }

    public ResCommon modifyChannelName(int id, String name) throws JandiException {
        ReqCreateCdp entityInfo = new ReqCreateCdp();
        entityInfo.name = name;
        try {
            return mJandiRestClient.modifyChannel(entityInfo, id);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
    }

    public ResCommon modifyPrivateGroupName(int id, String name) throws JandiException {
        ReqCreateCdp entityInfo = new ReqCreateCdp();
        entityInfo.name = name;
        try {
            return mJandiRestClient.modifyGroup(entityInfo, id);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
    }

    public ResCommon deleteChannel(int id) throws JandiException {
        try {
            return mJandiRestClient.deleteChannel(id);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
    }

    public ResCommon deletePrivateGroup(int id) throws JandiException {
        try {
            return mJandiRestClient.deleteGroup(id);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
    }

    public ResCommon inviteChannel(int id, List<Integer> invitedUsers) throws JandiException {
        ReqInviteUsers reqInviteUsers = new ReqInviteUsers(invitedUsers);
        try {
            return mJandiRestClient.inviteChannel(id, reqInviteUsers);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
    }

    public ResCommon invitePrivateGroup(int id, List<Integer> invitedUsers) throws JandiException {
        ReqInviteUsers reqInviteUsers = new ReqInviteUsers(invitedUsers);
        try {
            return mJandiRestClient.inviteGroup(id, reqInviteUsers);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
    }
}
