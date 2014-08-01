package com.tosslab.jandi.app.network;

import com.tosslab.jandi.app.network.models.ResAuthToken;
import com.tosslab.jandi.app.network.models.ReqLogin;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationSubscribe;
import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
import com.tosslab.jandi.app.network.models.ReqNotificationUpdate;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.utils.JandiException;

import org.springframework.web.client.HttpStatusCodeException;

/**
 * Created by justinygchoi on 2014. 7. 16..
 * TODO MessageManipulator 와 합쳐지겠지...
 */
public class JandiNetworkClient {
    TossRestClient mRestClient;

    public JandiNetworkClient(TossRestClient tossRestClient) {
        mRestClient = tossRestClient;
        mRestClient.setHeader("Accept", "application/vnd.tosslab.jandi-v1+json");
    }

    public ResMyTeam getMyTeamId(String id) throws JandiException {
        try {
            return mRestClient.getTeamId(id);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
    }

    public ResAuthToken login(int teamId, String id, String passwd) throws JandiException {
        ReqLogin reqLogin = new ReqLogin(teamId, id, passwd);
        try {
            return mRestClient.loginAndReturnToken(reqLogin);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
    }

    public void setAuthToken(String token) {
        mRestClient.setHeader("Authorization", token);
    }

    public ResCommon registerNotificationToken(String regId) throws JandiException {
        ReqNotificationRegister req = new ReqNotificationRegister("android", regId);
        try {
            return mRestClient.registerNotificationToken(req);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
    }

    public ResCommon updateNotificateionToken(String oldRegId, String newRegId) throws JandiException {
        ReqNotificationUpdate req = new ReqNotificationUpdate(newRegId);
        try {
            return mRestClient.updateNotificationToken(oldRegId, req);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
    }

    public ResCommon deleteNotificationToken(String regId) throws JandiException {
        try {
            return mRestClient.deleteNotificationToken(regId);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
    }

    public ResCommon subscribeNotification(String regId, boolean isSubscribe) throws JandiException {
        ReqNotificationSubscribe req = new ReqNotificationSubscribe(isSubscribe);

        try {
            return mRestClient.subscribeNotification(regId, req);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
    }

    public ResCommon setNotificationTarget(String target) throws JandiException {
        ReqNotificationTarget req = new ReqNotificationTarget(target);
        try {
            return mRestClient.setNotificationTarget(req);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
    }
}
