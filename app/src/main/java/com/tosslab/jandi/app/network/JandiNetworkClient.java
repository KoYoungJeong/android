package com.tosslab.jandi.app.network;

import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationSubscribe;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.utils.JandiException;

import org.springframework.web.client.HttpStatusCodeException;

/**
 * Created by justinygchoi on 2014. 7. 16..
 * TODO MessageManipulator 와 합쳐지겠지...
 */
public class JandiNetworkClient {
    TossRestClient mRestClient;

    public JandiNetworkClient(TossRestClient tossRestClient, String token) {
        mRestClient = tossRestClient;
        mRestClient.setHeader("Authorization", token);
    }

    public ResCommon registerNotificationToken(String regId) throws JandiException {
        ReqNotificationRegister req = new ReqNotificationRegister("android", regId);
        ResCommon res = null;
        try {
            res = mRestClient.registerNotificationToken(req);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
        return res;
    }

    public ResCommon subscribeNotification(String regId, boolean isSubscribe) throws JandiException {
        ReqNotificationSubscribe req = new ReqNotificationSubscribe(isSubscribe);
        ResCommon res = null;
        try {
            res = mRestClient.subscribeNotification(regId, req);
        } catch (HttpStatusCodeException e) {
            throw new JandiException(e);
        }
        return res;
    }
}
